locals {
  name_prefix = "${var.app_name}-${var.environment}"

  app_port           = 9081
  config_server_port = 8888
  rabbitmq_amqp_port = 5672
  rabbitmq_mgmt_port = 15672

  config_server_dns = "config-server.${local.name_prefix}.local"
  rabbitmq_dns      = "rabbitmq.${local.name_prefix}.local"

  db_jdbc_url = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}"

  common_tags = {
    Application = var.app_name
    Environment = var.environment
    ManagedBy   = "terraform"
    Project     = "iAC-NikeRuns"
  }
}
