resource "aws_db_subnet_group" "main" {
  name       = "${var.app_name}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = { Name = "${var.app_name}-db-subnet-group" }
}

resource "aws_db_instance" "app" {
  for_each = var.environments

  identifier        = "${var.app_name}-${each.key}-db"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"

  # Database name uses underscores (PostgreSQL identifier rules)
  db_name  = "${var.app_name}_${each.key}"
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  # Prod: backups + deletion protection. Dev/staging: minimal cost.
  backup_retention_period   = each.key == "prod" ? 7 : 1
  deletion_protection       = each.key == "prod"
  skip_final_snapshot       = each.key != "prod"
  final_snapshot_identifier = each.key == "prod" ? "${var.app_name}-prod-final-snapshot" : null

  # Maintenance window during low-traffic hours
  maintenance_window      = "Mon:03:00-Mon:04:00"
  backup_window           = "02:00-03:00"

  tags = {
    Name        = "${var.app_name}-${each.key}-db"
    Environment = each.key
  }
}
