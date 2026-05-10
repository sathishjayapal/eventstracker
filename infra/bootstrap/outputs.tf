output "state_bucket_name" {
  description = "S3 bucket name — use this in backend.hcl"
  value       = aws_s3_bucket.tf_state.id
}

output "state_lock_table_name" {
  description = "DynamoDB table name — use this in backend.hcl"
  value       = aws_dynamodb_table.tf_state_lock.name
}
