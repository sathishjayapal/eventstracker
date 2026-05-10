resource "aws_ecs_task_definition" "app" {
  family                   = "${local.name_prefix}-app"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.eventstracker_cpu
  memory                   = var.eventstracker_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "eventstracker"
    image     = "${aws_ecr_repository.app.repository_url}:${var.eventstracker_image_tag}"
    essential = true

    portMappings = [{ containerPort = local.app_port, protocol = "tcp" }]

    environment = [
      { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
      { name = "CONFIG_SERVER_URL",      value = "http://${local.config_server_dns}:${local.config_server_port}" },
      { name = "SPRING_RABBITMQ_HOST",   value = local.rabbitmq_dns },
      { name = "SPRING_RABBITMQ_PORT",   value = tostring(local.rabbitmq_amqp_port) },
    ]

    secrets = [
      { name = "SPRING_CLOUD_CONFIG_USERNAME", valueFrom = "${aws_secretsmanager_secret.config_server_auth.arn}:username::" },
      { name = "SPRING_CLOUD_CONFIG_PASSWORD", valueFrom = "${aws_secretsmanager_secret.config_server_auth.arn}:password::" },
      { name = "SPRING_DATASOURCE_URL",        valueFrom = "${aws_secretsmanager_secret.db.arn}:url::" },
      { name = "SPRING_DATASOURCE_USERNAME",   valueFrom = "${aws_secretsmanager_secret.db.arn}:username::" },
      { name = "SPRING_DATASOURCE_PASSWORD",   valueFrom = "${aws_secretsmanager_secret.db.arn}:password::" },
      { name = "SPRING_RABBITMQ_USERNAME",     valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:username::" },
      { name = "SPRING_RABBITMQ_PASSWORD",     valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:password::" },
      { name = "EVENT_DOMAIN_USER",            valueFrom = "${aws_secretsmanager_secret.app_user.arn}:username::" },
      { name = "EVENT_DOMAIN_USER_PASSWORD",   valueFrom = "${aws_secretsmanager_secret.app_user.arn}:password::" },
    ]

    healthCheck = {
      command     = ["CMD-SHELL", "curl -sf http://localhost:${local.app_port}/actuator/health || exit 1"]
      interval    = 30
      timeout     = 10
      retries     = 5
      startPeriod = 90
    }

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "eventstracker"
      }
    }
  }])
}

resource "aws_ecs_service" "app" {
  name                              = "${local.name_prefix}-app"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.app.arn
  desired_count                     = var.eventstracker_desired_count
  launch_type                       = "FARGATE"
  platform_version                  = "LATEST"
  health_check_grace_period_seconds = 120
  force_new_deployment              = true

  network_configuration {
    subnets          = data.aws_subnets.private.ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "eventstracker"
    container_port   = local.app_port
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  deployment_controller {
    type = "ECS"
  }

  depends_on = [
    aws_ecs_service.config_server,
    aws_ecs_service.rabbitmq,
    aws_lb_listener.http,
    aws_iam_role_policy_attachment.ecs_execution_managed,
  ]
}
