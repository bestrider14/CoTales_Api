resource "aws_ecs_cluster" "main" {
  name = "${var.app_name}-cluster"

  setting {
    name  = "containerInsights"
    value = "disabled" # Enable later for prod observability (adds ~$0.35/GB log ingested)
  }

  tags = { Name = "${var.app_name}-cluster" }
}

resource "aws_ecs_task_definition" "app" {
  for_each = var.environments

  family                   = "${var.app_name}-api-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn

  # Bootstrapped with nginx. CI/CD replaces this on first deploy.
  container_definitions = jsonencode([{
    name      = "cotales-api"
    image     = "nginx:alpine"
    essential = true
    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = "/ecs/${var.app_name}-api-${each.key}"
        awslogs-region        = var.aws_region
        awslogs-stream-prefix = "ecs"
      }
    }
  }])

  # CI/CD manages container_definitions and desired_count after initial creation.
  lifecycle {
    ignore_changes = [container_definitions]
  }

  depends_on = [aws_cloudwatch_log_group.app]
}

resource "aws_ecs_service" "app" {
  for_each = var.environments

  name            = "${var.app_name}-api-${each.key}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app[each.key].arn
  desired_count   = each.value.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true # Tasks in public subnets reach ECR/CloudWatch without a NAT Gateway
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app[each.key].arn
    container_name   = "cotales-api"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.http]

  # CI/CD updates task_definition on every deploy; Terraform must not revert it.
  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }

  tags = {
    Name        = "${var.app_name}-api-${each.key}"
    Environment = each.key
  }
}
