#!/bin/bash

# EventTracker Unified Control Script
# Handles: dependencies, local dev, config server, cloud setup, mappers
# Usage: ./eventtracker.sh [command] [options]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Helper functions (needed early for error messages)
print_status() { echo -e "${GREEN}✓${NC} $1"; }
print_error() { echo -e "${RED}✗${NC} $1"; }
print_info() { echo -e "${YELLOW}ℹ${NC} $1"; }
print_header() { echo -e "\n${BLUE}$1${NC}"; }

# Configuration
CONTAINER_NAME="event-service-db"
DB_PORT=6433
APP_PORT=9081

# Load .env file — required for all commands
if [ -f "$SCRIPT_DIR/.env" ]; then
    set -a
    source "$SCRIPT_DIR/.env"
    set +a
else
    print_error ".env file not found in $SCRIPT_DIR"
    print_info "Create a .env file with the required variables (see README)"
    exit 1
fi

# Validate required variables
require_env() {
    local var_name=$1
    if [ -z "${!var_name}" ]; then
        print_error "Required variable $var_name is not set in .env"
        exit 1
    fi
}

validate_env() {
    local missing=0
    for var in EVENTS_TRACKER_DB_URL EVENTS_TRACKER_DB_USER EVENTS_TRACKER_DB_PASSWORD \
               RABBITMQ_HOST RABBITMQ_PORT RABBITMQ_USERNAME RABBITMQ_PASSWORD \
               EVENT_DOMAIN_USER EVENT_DOMAIN_USER_PASSWORD; do
        if [ -z "${!var}" ]; then
            print_error "Missing required variable: $var"
            missing=$((missing + 1))
        fi
    done
    if [ $missing -gt 0 ]; then
        print_info "Check your .env file — all variables above are required"
        exit 1
    fi
}

# Extract DB connection parts from JDBC URL for Docker/pg_isready usage
DB_NAME=$(echo "$EVENTS_TRACKER_DB_URL" | sed -n 's|.*://[^/]*/\([^?]*\).*|\1|p')
DB_USER="$EVENTS_TRACKER_DB_USER"
DB_PASSWORD="$EVENTS_TRACKER_DB_PASSWORD"

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

find_directory() {
    local name=$1
    local paths=("${@:2}")

    for path in "${paths[@]}"; do
        if [ -d "$path" ]; then
            if [ -f "$path/.env" ] || [ -f "$path/docker-compose.yml" ]; then
                echo "$(cd "$path" && pwd)"
                return 0
            fi
        fi
    done

    return 1
}

find_consolidated_root() {
    local candidates=(
        "../consolidated-postgres"
        "../../consolidated-postgres"
        "$HOME/IdeaProjects/consolidated-postgres"
        "$HOME/Library/CloudStorage/GoogleDrive-satsmadison@gmail.com/Other computers/My Mac/consolidated-postgres"
    )

    for path in "${candidates[@]}"; do
        if [ -d "$path" ] && [ -d "$path/scripts/local" ]; then
            echo "$(cd "$path" && pwd)"
            return 0
        fi
    done

    # Not found locally — try to clone from GitHub into ~/IdeaProjects
    local clone_target="$HOME/IdeaProjects/consolidated-postgres"
    if command -v git &>/dev/null; then
        print_info "consolidated-postgres not found locally — cloning from GitHub..."
        if git clone https://github.com/sathishjayapal/consolidated-postgres.git "$clone_target" 2>/dev/null; then
            print_status "consolidated-postgres cloned to $clone_target"
            echo "$clone_target"
            return 0
        else
            print_error "Failed to clone consolidated-postgres from GitHub"
        fi
    fi

    return 1
}

sync_machine_local_envs() {
    local consolidated_root
    consolidated_root=$(find_consolidated_root || true)

    if [ -z "$consolidated_root" ]; then
        print_error "consolidated-postgres not found and could not be cloned from GitHub"
        print_info "Clone it manually: git clone https://github.com/sathishjayapal/consolidated-postgres.git \$HOME/IdeaProjects/consolidated-postgres"
        return 1
    fi

    local bootstrap_script="$consolidated_root/scripts/local/bootstrap-env.sh"
    if [ ! -x "$bootstrap_script" ]; then
        print_error "Expected executable bootstrap script: $bootstrap_script"
        print_info "Run: chmod 755 '$bootstrap_script'"
        return 1
    fi

    print_info "Syncing local env files from consolidated-postgres..."
    "$bootstrap_script"
}

# ============================================================================
# COMMAND: deps - Start only infrastructure (PostgreSQL, RabbitMQ, Config)
# ============================================================================

cleanup_stale_containers() {
    local containers=("sathish-config-server" "sathishproject-rabbitmq" "eventstracker-postgres")

    for container in "${containers[@]}"; do
        if docker ps -a --format '{{.Names}}' 2>/dev/null | grep -q "^${container}$"; then
            # Force remove stopped containers
            docker stop "$container" 2>/dev/null || true
            docker rm "$container" 2>/dev/null || true
            print_info "Cleaned up container: $container"
        fi
    done
}

cmd_deps() {
    print_header "Starting Infrastructure"

    sync_machine_local_envs

    # Find directories
    CONFIG_DIR=$(find_directory "config-server" \
        "../sathishproject-config-server" \
        "../../sathishproject-config-server" \
        "$HOME/IdeaProjects/sathishproject-config-server") || {
        print_error "Config server not found"
        return 1
    }

    INFRA_DIR=$(find_directory "infrastructure" \
        "../jubilant-memory/config" \
        "../../jubilant-memory/config" \
        "$HOME/IdeaProjects/jubilant-memory/config") || {
        print_error "Infrastructure directory not found"
        return 1
    }

    print_status "Found config server: $CONFIG_DIR"
    print_status "Found infrastructure: $INFRA_DIR"

    # Clean up stale containers before starting
    print_info "Checking for stale containers..."
    cleanup_stale_containers

    # Start config server (always recreate so credentials from .env are applied)
    print_info "Starting Config Server..."
    cd "$CONFIG_DIR"
    docker-compose --env-file .env up -d --force-recreate config-server

    # Read config-server auth values from its own .env
    CONFIG_USER=$(grep -E '^username=' .env | tail -1 | cut -d'=' -f2-)
    CONFIG_PASS=$(grep -E '^pass=' .env | tail -1 | cut -d'=' -f2-)
    if [ -z "$CONFIG_USER" ] || [ -z "$CONFIG_PASS" ]; then
        print_error "Config server username/pass missing in $CONFIG_DIR/.env"
        return 1
    fi

    # Wait for config server endpoint to be reachable with auth
    for i in {1..45}; do
        if curl -sf -u "$CONFIG_USER:$CONFIG_PASS" "http://localhost:8888/eventstracker/local" > /dev/null 2>&1; then
            print_status "Config Server ready and auth verified"
            break
        fi
        if [ "$i" -eq 45 ]; then
            print_error "Config Server did not become ready with valid credentials"
            print_info "Recent config-server logs:"
            docker logs --tail 40 sathish-config-server 2>/dev/null || true
            return 1
        fi
        echo -n "."
        sleep 1
    done

    # Start PostgreSQL via the consolidated local-db path.
    print_info "Starting PostgreSQL (single source of truth: dev-up.sh)..."
    cd "$SCRIPT_DIR"
    ./dev-up.sh

    # Start RabbitMQ with the infra env file (golden source for credentials).
    print_info "Starting RabbitMQ..."
    cd "$INFRA_DIR"
    docker-compose --env-file "$INFRA_DIR/.env" up -d sathishproject-rabbitmq

    cd "$SCRIPT_DIR"
    validate_env

    sleep 3
    print_status "Infrastructure started"
    print_info "Config Server: http://localhost:8888"
    print_info "PostgreSQL: localhost:$DB_PORT"
    print_info "RabbitMQ: localhost:$RABBITMQ_PORT"
    print_info "All env vars loaded from $SCRIPT_DIR/.env"
}

# ============================================================================
# COMMAND: dev - Local development setup (local DB, no config server)
# ============================================================================

cmd_dev() {
    print_header "Setting up Local Development Environment"

    sync_machine_local_envs

    if [ "${1:-}" = "--reset" ]; then
        print_info "Resetting EventTracker database via dev-up.sh..."
        ./dev-up.sh --reset
    else
        ./dev-up.sh
    fi

    validate_env
    print_status "Development environment ready"
    print_info "Run: mvn spring-boot:run -Dspring-boot.run.profiles=local"
}

# ============================================================================
# COMMAND: start - Full stack (config server + deps + app)
# ============================================================================

cmd_start() {
    print_header "Starting Full Stack"

    cmd_deps

    validate_env

    print_header "Starting EventTracker"
    export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-local}
    export CONFIG_SERVER_URL=${CONFIG_SERVER_URL:-http://localhost:8888}

    print_info "Profile: $SPRING_PROFILES_ACTIVE"
    print_info "Config Server: $CONFIG_SERVER_URL"

    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Please ensure Maven is installed"
        return 1
    fi

    print_info "Downloading dependencies (first time only)..."
    if ! mvn dependency:resolve -q 2>/dev/null; then
        print_info "Dependency resolution in progress..."
    fi

    print_info "Running: mvn spring-boot:run -Dspring-boot.run.profiles=local"
    mvn spring-boot:run -Dspring-boot.run.profiles=local -DaddResources || {
        print_error "Application startup failed"
        print_info "Check your configuration and database connectivity"
        return 1
    }
}

# ============================================================================
# COMMAND: mappers - Generate MapStruct implementations
# ============================================================================

cmd_mappers() {
    print_header "Generating MapStruct Implementations"

    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Please ensure Maven is installed and in PATH"
        return 1
    fi

    print_info "Cleaning project..."
    if ! mvn clean -q; then
        print_error "Maven clean failed"
        return 1
    fi

    print_info "Compiling with MapStruct..."
    if ! mvn compile -DskipTests -q; then
        print_error "Maven compile failed"
        return 1
    fi

    if [ -d "target/generated-sources/annotations" ]; then
        print_status "Generated sources found"
        if find target/generated-sources/annotations -name "*MapperImpl.java" -type f | grep -q .; then
            print_status "MapStruct implementations generated successfully"
        else
            print_error "No MapperImpl.java files found"
            return 1
        fi
    else
        print_error "Generated sources directory not created"
        return 1
    fi
}

# ============================================================================
# COMMAND: cloud - Configure for cloud database
# ============================================================================

cmd_cloud() {
    print_header "Configuring Cloud Database"

    local cloud_env="../consolidated-postgres/.env.cloud"

    if [ ! -f "$cloud_env" ]; then
        print_error "Cloud configuration not found at $cloud_env"
        return 1
    fi

    local jdbc_url db_user db_pass
    jdbc_url=$(grep "^EVENTS_TRACKER_DB_URL=" "$cloud_env" | cut -d'=' -f2-)
    db_user=$(grep "^EVENTS_TRACKER_DB_USER=" "$cloud_env" | cut -d'=' -f2-)
    db_pass=$(grep "^EVENTS_TRACKER_DB_PASSWORD=" "$cloud_env" | cut -d'=' -f2-)

    if [ -z "$jdbc_url" ] || [ -z "$db_user" ] || [ -z "$db_pass" ]; then
        print_error "Missing EVENTS_TRACKER_DB_URL/USER/PASSWORD in $cloud_env — run cloud-start.sh first"
        return 1
    fi

    # Preserve existing app-level secrets from current .env
    local saved_event_user saved_event_pass saved_rabbit_user saved_rabbit_pass
    saved_event_user=$(grep -E "^EVENT_DOMAIN_USER=" .env 2>/dev/null | cut -d'=' -f2- || true)
    saved_event_pass=$(grep -E "^EVENT_DOMAIN_USER_PASSWORD=" .env 2>/dev/null | cut -d'=' -f2- || true)
    saved_rabbit_user=$(grep -E "^RABBITMQ_USERNAME=" .env 2>/dev/null | cut -d'=' -f2- || true)
    saved_rabbit_pass=$(grep -E "^RABBITMQ_PASSWORD=" .env 2>/dev/null | cut -d'=' -f2- || true)

    local rabbit_user rabbit_pass
    rabbit_user=${RABBITMQ_USERNAME:-$saved_rabbit_user}
    rabbit_pass=${RABBITMQ_PASSWORD:-$saved_rabbit_pass}
    if [ -z "$rabbit_user" ] || [ -z "$rabbit_pass" ]; then
        print_error "RABBITMQ_USERNAME/PASSWORD must be set (export vars or define in current .env)"
        return 1
    fi

    cat > .env << EOF
# Cloud Database Configuration — generated by eventtracker.sh cloud
EVENTS_TRACKER_DB_URL=$jdbc_url
EVENTS_TRACKER_DB_USER=$db_user
EVENTS_TRACKER_DB_PASSWORD=$db_pass
RABBITMQ_HOST=${RABBITMQ_HOST:-localhost}
RABBITMQ_PORT=${RABBITMQ_PORT:-5672}
RABBITMQ_USERNAME=${rabbit_user}
RABBITMQ_PASSWORD=${rabbit_pass}
EVENT_DOMAIN_USER=${saved_event_user}
EVENT_DOMAIN_USER_PASSWORD=${saved_event_pass}
EOF
    chmod 600 .env

    print_status "Cloud configuration ready"
    print_info "Run: SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run"
}

# ============================================================================
# COMMAND: stop - Stop all containers
# ============================================================================

cmd_stop() {
    print_header "Stopping Containers"

    docker compose down 2>/dev/null && print_status "Local containers stopped" || true
    docker stop sathish-config-server 2>/dev/null && print_status "Config server stopped" || true
    docker stop event-service-db 2>/dev/null && print_status "PostgreSQL stopped" || true
    docker stop sathishproject-rabbitmq 2>/dev/null && print_status "RabbitMQ stopped" || true
}

# ============================================================================
# COMMAND: status - Check status of services
# ============================================================================

cmd_status() {
    print_header "Service Status"

    echo "Docker Services:"
    docker ps --format "table {{.Names}}\t{{.Status}}" 2>/dev/null | grep -E "(sathish|event|postgres|rabbit)" || print_info "No services running"

    echo ""
    echo "Port Status:"
    for port in 5432 6433 5672 8888 9081; do
        if nc -z localhost $port 2>/dev/null; then
            print_status "Port $port is in use"
        else
            print_info "Port $port is free"
        fi
    done
}

# ============================================================================
# COMMAND: check - Validate all dependencies
# ============================================================================

cmd_check() {
    print_header "Dependency Check"

    local missing=0

    # Check Docker
    if command -v docker &> /dev/null; then
        print_status "Docker is installed"
        if docker ps > /dev/null 2>&1; then
            print_status "Docker is running"
        else
            print_error "Docker is not running"
            missing=$((missing + 1))
        fi
    else
        print_error "Docker not found"
        missing=$((missing + 1))
    fi

    # Check Maven
    if command -v mvn &> /dev/null; then
        print_status "Maven is installed"
    else
        print_error "Maven not found"
        missing=$((missing + 1))
    fi

    # Check Java
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | head -1)
        print_status "Java is installed: $java_version"
    else
        print_error "Java not found"
        missing=$((missing + 1))
    fi

    # Check project structure
    if [ -f "pom.xml" ]; then
        print_status "pom.xml found"
    else
        print_error "pom.xml not found"
        missing=$((missing + 1))
    fi

    if [ -f "docker-compose.yml" ]; then
        print_status "docker-compose.yml found"
    else
        print_error "docker-compose.yml not found"
        missing=$((missing + 1))
    fi

    # Check directories
    if [ -d "src/main/java" ]; then
        print_status "src/main/java directory found"
    else
        print_error "src/main/java directory not found"
        missing=$((missing + 1))
    fi

    echo ""
    if [ $missing -eq 0 ]; then
        print_status "All dependencies are met!"
        return 0
    else
        print_error "$missing dependency/dependencies missing"
        return 1
    fi
}

# ============================================================================
# MAIN MENU / HELP
# ============================================================================

show_help() {
    cat << EOF
${BLUE}EventTracker - Unified Control Script${NC}

${YELLOW}USAGE:${NC}
    ./eventtracker.sh [command] [options]

${YELLOW}COMMANDS:${NC}
    check       Validate all dependencies (run this first!)

    dev         Start local dev environment (DB via dev-up.sh, no config server)
                Options: --reset (drop and recreate database)

    deps        Start only infrastructure (Config Server + DB/RabbitMQ from infra .env)

    start       Start full stack (config server + deps + app)

    mappers     Generate MapStruct implementations

    cloud       Configure for cloud database

    stop        Stop all containers

    status      Check status of services

    help        Show this message

${YELLOW}EXAMPLES:${NC}
    # Local development
    ./eventtracker.sh dev
    ./eventtracker.sh dev --reset

    # Full stack with config server
    ./eventtracker.sh deps
    ./eventtracker.sh start

    # Cloud setup
    ./eventtracker.sh cloud

    # Development tools
    ./eventtracker.sh mappers

${YELLOW}PORTS:${NC}
    5432   - PostgreSQL (local dev)
    6433   - PostgreSQL (infrastructure)
    5672   - RabbitMQ
    8888   - Config Server
    9081   - EventTracker Application

${YELLOW}NEXT STEPS:${NC}
    1. ./eventtracker.sh check         # Verify dependencies
    2. ./eventtracker.sh dev           # Set up local DB
    3. mvn spring-boot:run             # Or use IDE

    OR for full stack with config server:

    1. ./eventtracker.sh check         # Verify dependencies
    2. ./eventtracker.sh start         # Starts everything

${YELLOW}TROUBLESHOOTING:${NC}
    If you get "No plugin found for prefix 'spring-boot'":
    1. Run: mvn clean install -DskipTests
    2. Then: ./eventtracker.sh start

    If containers won't start:
    1. Run: ./eventtracker.sh stop     # Stop all containers
    2. Check: ./eventtracker.sh status  # Check ports

    If database issues:
    1. Run: ./eventtracker.sh dev --reset

${YELLOW}SUPPORT COMMANDS:${NC}
    ./eventtracker.sh check            # Validate setup
    ./eventtracker.sh status           # Check running services
    ./eventtracker.sh stop             # Stop all containers
EOF
}

# ============================================================================
# MAIN
# ============================================================================

COMMAND=${1:-help}

case "$COMMAND" in
    check)
        cmd_check
        ;;
    dev)
        cmd_check && cmd_dev "$2"
        ;;
    deps)
        cmd_check && cmd_deps
        ;;
    start)
        cmd_check && cmd_start
        ;;
    mappers)
        cmd_check && cmd_mappers
        ;;
    cloud)
        cmd_check && cmd_cloud
        ;;
    stop)
        cmd_stop
        ;;
    status)
        cmd_status
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        echo ""
        show_help
        exit 1
        ;;
esac
