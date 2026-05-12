variable "aws_region" {
  description = "AWS region to deploy to (e.g. eu-west-1)"
  type        = string
}

variable "app_name" {
  description = "Application name prefix for all resources"
  type        = string
  default     = "cotales"
}

variable "environments" {
  description = "Per-environment ECS sizing and ALB routing priority"
  type = map(object({
    cpu           = number
    memory        = number
    desired_count = number
    alb_priority  = number
  }))
  default = {
    dev = {
      cpu           = 256
      memory        = 512
      desired_count = 1
      alb_priority  = 100
    }
    staging = {
      cpu           = 256
      memory        = 512
      desired_count = 1
      alb_priority  = 200
    }
    prod = {
      cpu           = 512
      memory        = 1024
      desired_count = 1
      alb_priority  = 300
    }
  }
}

variable "domain_names" {
  description = "Hostname per environment for ALB host-based routing"
  type        = map(string)
  default = {
    dev     = "dev-api.example.com"
    staging = "staging-api.example.com"
    prod    = "api.example.com"
  }
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "cotales"
}

variable "db_password" {
  description = "RDS master password (min 8 chars)"
  type        = string
  sensitive   = true
}

variable "acm_certificate_arn" {
  description = "ACM certificate ARN for HTTPS. Leave empty to use HTTP only."
  type        = string
  default     = ""
}
