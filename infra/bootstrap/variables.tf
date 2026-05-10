variable "aws_region" {
  description = "AWS region for the Terraform state backend resources"
  type        = string
  default     = "us-east-1"
}

variable "state_bucket_name" {
  description = "Name of the S3 bucket that will store Terraform state"
  type        = string
}

variable "state_lock_table_name" {
  description = "Name of the DynamoDB table used for Terraform state locking"
  type        = string
  default     = "terraform-state-lock"
}

variable "project" {
  description = "Project name used to tag bootstrap resources"
  type        = string
  default     = "eventstracker"
}
