output "server_ip" {
  description = "GitHub secret: EC2_HOST"
  value       = aws_eip.app.public_ip
}

output "ec2_private_key_pem" {
  description = "Run: terraform output -raw ec2_private_key_pem > cotales.pem && chmod 600 cotales.pem"
  value       = tls_private_key.ec2.private_key_pem
  sensitive   = true
}

output "ecr_registry" {
  description = "ECR registry hostname (account + region)"
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
  description = "GitHub secret: AWS_SECRET_ACCESS_KEY"
  value       = aws_iam_access_key.github_actions.secret
  sensitive   = true
}

data "aws_caller_identity" "current" {}
