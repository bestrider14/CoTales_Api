resource "aws_cloudwatch_log_group" "app" {
  for_each = var.environments

  name              = "/ecs/${var.app_name}-api-${each.key}"
  retention_in_days = each.key == "prod" ? 30 : 7

  tags = { Environment = each.key }
}
