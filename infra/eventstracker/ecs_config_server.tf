resource "aws_ecs_task_definition" "config_server" {
  family                   = "${local.name_prefix}-config-server"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.config_server_cpu
  memory                   = var.config_server_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "config-server"
    image     = "${aws_ecr_repository.config_server.repository_url}:${var.config_server_image_tag}"
    essential = true

    portMappings = [{ containerPort = local.config_server_port, protocol = "tcp" }]

    secrets = [
      { name = "GIT_URI",                      valueFrom = "${aws_secretsmanager_secret.config_server_git.arn}:uri::" },
      { name = "username",                     valueFrom = "${aws_secretsmanager_secret.config_server_git.arn}:username::" },
      { name = "pass",                         valueFrom = "${aws_secretsmanager_secret.config_server_git.arn}:password::" },
      { name = "encrypt_key",                  valueFrom = "${aws_secretsmanager_secret.config_server_git.arn}:encrypt_key::" },
      { name = "SPRING_SECURITY_USER_NAME",     valueFrom = "${aws_secretsmanager_secret.config_server_auth.arn}:username::" },
      { name = "SPRING_SECURITY_USER_PASSWORD", valueFrom = "${aws_secretsmanager_secret.config_server_auth.arn}:password::" },
    ]

    environment = [
      { name = "APP_PORT",              value = tostring(local.config_server_port) },
      { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
    ]

    healthCheck = {
      command     = ["CMD-SHELL", "curl -sf http://localhost:${local.config_server_port}/actuator/health || exit 1"]
      interval    = 30
      timeout     = 10
      retries     = 5
      startPeriod = 60
    }

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.config_server.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "config-server"
      }
    }
  }])
}

resource "aws_ecs_service" "config_server" {
  name                              = "${local.name_prefix}-config-server"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.config_server.arn
  desired_count                     = 1
  launch_type                       = "FARGATE"
  platform_version                  = "LATEST"
  health_check_grace_period_seconds = 90
  force_new_deployment              = true

  network_configuration {
    subnets          = data.aws_subnets.private.ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.config_server.arn
  }

  depends_on = [aws_iam_role_policy_attachment.ecs_execution_managed]
}
