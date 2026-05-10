# ── AWS ───────────────────────────────────────────────────────────────────────
variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

# ── Naming / tagging ──────────────────────────────────────────────────────────
variable "environment" {
  description = "Deployment environment (prod, staging, dev)"
  type        = string
  default     = "prod"
}

variable "app_name" {
  description = "Application name — used as a prefix on all resource names"
  type        = string
  default     = "eventstracker"
}

# ── VPC / subnet discovery ────────────────────────────────────────────────────
variable "vpc_name_tag" {
  description = "Value of the 'Name' tag on the existing VPC to deploy into"
  type        = string
}

variable "private_subnet_tag_key" {
  description = "Tag key used to identify private subnets"
  type        = string
  default     = "Tier"
}

variable "private_subnet_tag_value" {
  description = "Tag value used to identify private subnets"
  type        = string
  default     = "Private"
}

variable "public_subnet_tag_key" {
  description = "Tag key used to identify public subnets"
  type        = string
  default     = "Tier"
}

variable "public_subnet_tag_value" {
  description = "Tag value used to identify public subnets"
  type        = string
  default     = "Public"
}

# ── RDS ───────────────────────────────────────────────────────────────────────
variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "eventstracker_db"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "eventstracker"
}

variable "db_password" {
  description = "PostgreSQL master password"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance type"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Initial RDS storage in GiB"
  type        = number
  default     = 20
}

variable "db_backup_retention_days" {
  description = "Number of days to retain automated RDS backups"
  type        = number
  default     = 7
}

# ── Spring Cloud Config Server ────────────────────────────────────────────────
variable "config_server_git_uri" {
  description = "Git repository URI that backs the Spring Cloud Config Server"
  type        = string
}

variable "config_server_git_username" {
  description = "Git username (or PAT) used by the config server"
  type        = string
}

variable "config_server_git_password" {
  description = "Git password or personal access token for the config server repo"
  type        = string
  sensitive   = true
}

variable "config_server_encrypt_key" {
  description = "Symmetric encryption key used by Spring Cloud Config Server"
  type        = string
  sensitive   = true
}

variable "config_server_username" {
  description = "HTTP Basic auth username exposed by the config server"
  type        = string
}

variable "config_server_password" {
  description = "HTTP Basic auth password exposed by the config server"
  type        = string
  sensitive   = true
}

# ── Application credentials ───────────────────────────────────────────────────
variable "event_domain_user" {
  description = "Default application user seeded into event_domain_user table"
  type        = string
}

variable "event_domain_user_password" {
  description = "Password for the default application user"
  type        = string
  sensitive   = true
}

# ── RabbitMQ ──────────────────────────────────────────────────────────────────
variable "rabbitmq_username" {
  description = "RabbitMQ admin username"
  type        = string
  default     = "admin"
}

variable "rabbitmq_password" {
  description = "RabbitMQ admin password"
  type        = string
  sensitive   = true
}

# ── Container images ──────────────────────────────────────────────────────────
variable "eventstracker_image_tag" {
  description = "Docker image tag to deploy for eventstracker"
  type        = string
  default     = "latest"
}

variable "config_server_image_tag" {
  description = "Docker image tag to deploy for the config server"
  type        = string
  default     = "latest"
}

variable "rabbitmq_image" {
  description = "RabbitMQ Docker image including tag"
  type        = string
  default     = "rabbitmq:3-management-alpine"
}

# ── ECS sizing ────────────────────────────────────────────────────────────────
variable "eventstracker_cpu" {
  type    = number
  default = 1024
}

variable "eventstracker_memory" {
  type    = number
  default = 2048
}

variable "eventstracker_desired_count" {
  type    = number
  default = 2
}

variable "config_server_cpu" {
  type    = number
  default = 512
}

variable "config_server_memory" {
  type    = number
  default = 1024
}

variable "rabbitmq_cpu" {
  type    = number
  default = 512
}

variable "rabbitmq_memory" {
  type    = number
  default = 1024
}

# ── ALB ───────────────────────────────────────────────────────────────────────
variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS on the public ALB. Leave empty for HTTP only."
  type        = string
  default     = ""
}
