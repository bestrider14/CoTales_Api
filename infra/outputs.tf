output "alb_dns_name" {
  description = "Point your domain A records here (dev-api / staging-api / api)"
  value       = aws_lb.main.dns_name
}

output "ecr_repository_url" {
  description = "Full ECR repository URL"
  value       = aws_ecr_repository.app.repository_url
}

output "ecr_registry" {
  description = "GitHub secret: AWS_REGION (also used to derive ECR registry host)"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

output "ecr_repository_name" {
  description = "GitHub secret: ECR_REPOSITORY"
  value       = aws_ecr_repository.app.name
}

output "github_actions_access_key_id" {
  description = "GitHub secret: AWS_ACCESS_KEY_ID"
  value       = aws_iam_access_key.github_actions.id
}

output "github_actions_secret_access_key" {
  description = "GitHub secret: AWS_SECRET_ACCESS_KEY  (sensitive — run: terraform output github_actions_secret_access_key)"
  value       = aws_iam_access_key.github_actions.secret
  sensitive   = true
}

output "ecs_cluster_name" {
  description = "GitHub secret: ECS_CLUSTER (same value for all environments)"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_names" {
  description = "GitHub secret: ECS_SERVICE per environment"
  value       = { for env, svc in aws_ecs_service.app : env => svc.name }
}

output "ecs_task_definition_families" {
  description = "GitHub secret: ECS_TASK_DEFINITION per environment"
  value       = { for env, td in aws_ecs_task_definition.app : env => td.family }
}

output "rds_endpoints" {
  description = "Use these to build DATASOURCE_URL: jdbc:postgresql://<endpoint>/<db_name>"
  value       = { for env, db in aws_db_instance.app : env => db.endpoint }
}

output "rds_database_names" {
  description = "Database name per environment"
  value       = { for env, db in aws_db_instance.app : env => db.db_name }
}

# Required for the ecr_registry output
data "aws_caller_identity" "current" {}
