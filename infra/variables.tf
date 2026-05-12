variable "aws_region" {
  description = "AWS region (e.g. eu-west-1)"
  type        = string
}

variable "app_name" {
  description = "Application name prefix for all resources"
  type        = string
  default     = "cotales"
}

variable "ssh_cidr" {
  description = "CIDR allowed to SSH into the server. Restrict to your own IP for safety (e.g. 1.2.3.4/32)."
  type        = string
  default     = "0.0.0.0/0"
}
