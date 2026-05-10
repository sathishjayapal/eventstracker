resource "aws_efs_file_system" "rabbitmq" {
  creation_token   = "${local.name_prefix}-rabbitmq"
  encrypted        = true
  performance_mode = "generalPurpose"
  throughput_mode  = "bursting"

  lifecycle_policy {
    transition_to_ia = "AFTER_30_DAYS"
  }
}

resource "aws_efs_mount_target" "rabbitmq" {
  for_each = toset(data.aws_subnets.private.ids)

  file_system_id  = aws_efs_file_system.rabbitmq.id
  subnet_id       = each.value
  security_groups = [aws_security_group.efs.id]
}

resource "aws_efs_access_point" "rabbitmq" {
  file_system_id = aws_efs_file_system.rabbitmq.id

  posix_user {
    uid = 999
    gid = 999
  }

  root_directory {
    path = "/rabbitmq"
    creation_info {
      owner_uid   = 999
      owner_gid   = 999
      permissions = "755"
    }
  }
}
