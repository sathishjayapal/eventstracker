resource "aws_secretsmanager_secret" "db" {
  name                    = "/${var.app_name}/${var.environment}/db"
  description             = "RDS PostgreSQL credentials and JDBC URL for ${local.name_prefix}"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
    url      = local.db_jdbc_url
  })
  depends_on = [aws_db_instance.postgres]
}

resource "aws_secretsmanager_secret" "config_server_git" {
  name                    = "/${var.app_name}/${var.environment}/config-server/git"
  description             = "Git credentials used by the Spring Cloud Config Server"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "config_server_git" {
  secret_id = aws_secretsmanager_secret.config_server_git.id
  secret_string = jsonencode({
    uri         = var.config_server_git_uri
    username    = var.config_server_git_username
    password    = var.config_server_git_password
    encrypt_key = var.config_server_encrypt_key
  })
}

resource "aws_secretsmanager_secret" "config_server_auth" {
  name                    = "/${var.app_name}/${var.environment}/config-server/auth"
  description             = "HTTP Basic auth credentials for the Spring Cloud Config Server API"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "config_server_auth" {
  secret_id = aws_secretsmanager_secret.config_server_auth.id
  secret_string = jsonencode({
    username = var.config_server_username
    password = var.config_server_password
  })
}

resource "aws_secretsmanager_secret" "rabbitmq" {
  name                    = "/${var.app_name}/${var.environment}/rabbitmq"
  description             = "RabbitMQ admin credentials for ${local.name_prefix}"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "rabbitmq" {
  secret_id = aws_secretsmanager_secret.rabbitmq.id
  secret_string = jsonencode({
    username = var.rabbitmq_username
    password = var.rabbitmq_password
  })
}

resource "aws_secretsmanager_secret" "app_user" {
  name                    = "/${var.app_name}/${var.environment}/app-user"
  description             = "Default EventTracker application user credentials"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "app_user" {
  secret_id = aws_secretsmanager_secret.app_user.id
  secret_string = jsonencode({
    username = var.event_domain_user
    password = var.event_domain_user_password
  })
}
