output "alb_dns_name" {
  description = "Public DNS name of the Application Load Balancer"
  value       = aws_lb.public.dns_name
}

output "alb_zone_id" {
  description = "Route 53 zone ID of the ALB (for alias records)"
  value       = aws_lb.public.zone_id
}

output "ecr_app_url" {
  description = "ECR repository URL for the eventstracker image"
  value       = aws_ecr_repository.app.repository_url
}

output "ecr_config_server_url" {
  description = "ECR repository URL for the config-server image"
  value       = aws_ecr_repository.config_server.repository_url
}

output "rds_endpoint" {
  description = "RDS instance endpoint (host:port)"
  value       = "${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}"
}

output "rds_db_name" {
  value = aws_db_instance.postgres.db_name
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "cloudmap_namespace" {
  value = aws_service_discovery_private_dns_namespace.app.name
}

output "vpc_id" {
  value = data.aws_vpc.existing.id
}
