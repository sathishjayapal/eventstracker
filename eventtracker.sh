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

# Configuration
CONTAINER_NAME="event-service-db"
DB_NAME="event-service"
DB_USER="psqladmin"
DB_PASSWORD="psqladminpas$"
DB_PORT=6433
APP_PORT=9081

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

print_status() { echo -e "${GREEN}✓${NC} $1"; }
print_error() { echo -e "${RED}✗${NC} $1"; }
print_info() { echo -e "${YELLOW}ℹ${NC} $1"; }
print_header() { echo -e "\n${BLUE}$1${NC}"; }

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

# ============================================================================
# COMMAND: deps - Start only infrastructure (PostgreSQL, RabbitMQ, Config)
# ============================================================================

cleanup_stale_containers() {
    local containers=("sathish-config-server" "event-service-db" "sathishproject-rabbitmq" "eventstracker-postgres")

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

    # Load environment
    print_info "Loading environment variables..."
    set -a
    source "$CONFIG_DIR/.env"
    set +a

    # Clean up stale containers before starting
    print_info "Checking for stale containers..."
    cleanup_stale_containers

    # Start config server
    print_info "Starting Config Server..."
    cd "$CONFIG_DIR"
    docker-compose --env-file .env up -d config-server

    # Wait for config server
    for i in {1..30}; do
        if curl -s http://localhost:8888/actuator/health > /dev/null 2>&1; then
            print_status "Config Server ready"
            break
        fi
        echo -n "."
        sleep 1
    done

    # Start infrastructure
    print_info "Starting PostgreSQL and RabbitMQ..."
    cd "$INFRA_DIR"
    docker-compose --env-file "$CONFIG_DIR/.env" up -d postgres sathishproject-rabbitmq

    sleep 3
    print_status "Infrastructure started"
    print_info "Config Server: http://localhost:8888"
    print_info "PostgreSQL: localhost:6433"
    print_info "RabbitMQ: localhost:5672"
}

# ============================================================================
# COMMAND: dev - Local development setup (local DB, no config server)
# ============================================================================

cmd_dev() {
    local reset=${1:-false}

    print_header "Setting up Local Development Environment"

    if ! docker ps > /dev/null 2>&1; then
        print_error "Docker is not running"
        return 1
    fi

    # Clean up stale containers
    print_info "Checking for stale containers..."
    cleanup_stale_containers

    if [ "$reset" = "--reset" ]; then
        print_info "Resetting database..."
        docker compose down -v 2>/dev/null || true
    fi

    print_info "Starting PostgreSQL..."
    if docker container inspect "$CONTAINER_NAME" > /dev/null 2>&1 && \
       docker ps --filter "name=$CONTAINER_NAME" | grep -q "$CONTAINER_NAME"; then
        print_status "PostgreSQL already running on port $DB_PORT"
    else
        docker compose up -d
        print_status "PostgreSQL started"
    fi

    # Wait for PostgreSQL
    print_info "Waiting for PostgreSQL..."
    for i in {1..30}; do
        if docker exec "$CONTAINER_NAME" pg_isready -U "$DB_USER" > /dev/null 2>&1; then
            print_status "PostgreSQL ready"
            break
        fi
        sleep 1
    done

    # Create .env file with config server and database credentials
    cat > .env << EOF
DB_HOST=localhost
DB_PORT=$DB_PORT
DB_NAME=$DB_NAME
DB_USERNAME=$DB_USER
DB_PASSWORD=$DB_PASSWORD
JDBC_DATABASE_URL=jdbc:postgresql://localhost:$DB_PORT/$DB_NAME
eventstracker_JDBC_DATABASE_URL=jdbc:postgresql://localhost:$DB_PORT/$DB_NAME
eventstracker_JDBC_DATABASE_USERNAME=$DB_USER
eventstracker_JDBC_DATABASE_PASSWORD=$DB_PASSWORD
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
SERVER_PORT=$APP_PORT
username=sathish
pass=pass
SPRING_CLOUD_CONFIG_USERNAME=sathish
SPRING_CLOUD_CONFIG_PASSWORD=pass
CONFIG_SERVER_URL=http://localhost:8888
SPRING_PROFILES_ACTIVE=local
EOF

    print_status ".env file created"
    print_status "Development environment ready"
    print_info "Run: mvn spring-boot:run"
}

# ============================================================================
# COMMAND: start - Full stack (config server + deps + app)
# ============================================================================

cmd_start() {
    print_header "Starting Full Stack"

    cmd_deps

    # Load .env file for config server credentials
    if [ -f ".env" ]; then
        print_info "Loading credentials from .env"
        set -a
        source .env
        set +a
    fi

    print_header "Starting EventTracker"
    export SPRING_PROFILES_ACTIVE=local
    export CONFIG_SERVER_URL=http://localhost:8888
    export SPRING_CLOUD_CONFIG_USERNAME=${username:-sathish}
    export SPRING_CLOUD_CONFIG_PASSWORD=${pass:-pass}

    print_info "Profile: $SPRING_PROFILES_ACTIVE"
    print_info "Config Server: $CONFIG_SERVER_URL"
    print_info "Config Server Auth: $SPRING_CLOUD_CONFIG_USERNAME"

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

    local jdbc_url username password
    jdbc_url=$(grep "^EVENTSTRACKER_DATASOURCE_URL=" "$cloud_env" | cut -d'=' -f2-)
    username=$(grep "^EVENTSTRACKER_DATASOURCE_USERNAME=" "$cloud_env" | cut -d'=' -f2-)
    password=$(grep "^EVENTSTRACKER_DATASOURCE_PASSWORD=" "$cloud_env" | cut -d'=' -f2-)

    if [ -z "$jdbc_url" ] || [ -z "$username" ] || [ -z "$password" ]; then
        print_error "Missing cloud configuration values"
        return 1
    fi

    cat > .env << EOF
# Cloud Database Configuration
JDBC_DATABASE_URL=$jdbc_url
eventstracker_JDBC_DATABASE_URL=$jdbc_url
eventstracker_JDBC_DATABASE_USERNAME=$username
eventstracker_JDBC_DATABASE_PASSWORD=$password
RABBITMQ_HOST=${RABBITMQ_HOST:-localhost}
RABBITMQ_PORT=${RABBITMQ_PORT:-5672}
RABBITMQ_USERNAME=${RABBITMQ_USERNAME:-guest}
RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD:-guest}
SERVER_PORT=$APP_PORT
EOF

    print_status "Cloud configuration ready"
    print_info "Run: mvn spring-boot:run"
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

    dev         Start local dev environment (local DB, no config server)
                Options: --reset (drop and recreate database)

    deps        Start only infrastructure (Config Server, PostgreSQL, RabbitMQ)

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
