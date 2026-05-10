resource "aws_service_discovery_private_dns_namespace" "app" {
  name        = "${local.name_prefix}.local"
  description = "Internal service discovery for ${local.name_prefix}"
  vpc         = data.aws_vpc.existing.id
}

resource "aws_service_discovery_service" "config_server" {
  name = "config-server"

  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.app.id
    routing_policy = "MULTIVALUE"
    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_service" "rabbitmq" {
  name = "rabbitmq"

  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.app.id
    routing_policy = "MULTIVALUE"
    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}
