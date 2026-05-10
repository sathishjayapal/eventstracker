resource "aws_ecs_task_definition" "rabbitmq" {
  family                   = "${local.name_prefix}-rabbitmq"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.rabbitmq_cpu
  memory                   = var.rabbitmq_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  volume {
    name = "rabbitmq-data"
    efs_volume_configuration {
      file_system_id     = aws_efs_file_system.rabbitmq.id
      transit_encryption = "ENABLED"
      authorization_config {
        access_point_id = aws_efs_access_point.rabbitmq.id
        iam             = "DISABLED"
      }
    }
  }

  container_definitions = jsonencode([{
    name      = "rabbitmq"
    image     = var.rabbitmq_image
    essential = true

    portMappings = [
      { containerPort = local.rabbitmq_amqp_port, protocol = "tcp" },
      { containerPort = local.rabbitmq_mgmt_port, protocol = "tcp" },
    ]

    secrets = [
      { name = "RABBITMQ_DEFAULT_USER", valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:username::" },
      { name = "RABBITMQ_DEFAULT_PASS", valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:password::" },
    ]

    mountPoints = [{ sourceVolume = "rabbitmq-data", containerPath = "/var/lib/rabbitmq", readOnly = false }]

    healthCheck = {
      command     = ["CMD-SHELL", "rabbitmq-diagnostics -q check_running || exit 1"]
      interval    = 30
      timeout     = 10
      retries     = 5
      startPeriod = 45
    }

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.rabbitmq.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "rabbitmq"
      }
    }
  }])

  depends_on = [aws_efs_mount_target.rabbitmq]
}

resource "aws_ecs_service" "rabbitmq" {
  name             = "${local.name_prefix}-rabbitmq"
  cluster          = aws_ecs_cluster.main.id
  task_definition  = aws_ecs_task_definition.rabbitmq.arn
  desired_count    = 1
  launch_type      = "FARGATE"
  platform_version = "1.4.0"

  network_configuration {
    subnets          = data.aws_subnets.private.ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.rabbitmq.arn
  }

  depends_on = [aws_iam_role_policy_attachment.ecs_execution_managed]
}
