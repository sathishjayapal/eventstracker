resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name_prefix}/eventstracker"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "config_server" {
  name              = "/ecs/${local.name_prefix}/config-server"
  retention_in_days = 14
}

resource "aws_cloudwatch_log_group" "rabbitmq" {
  name              = "/ecs/${local.name_prefix}/rabbitmq"
  retention_in_days = 14
}
