resource "aws_lb" "main" {
  name               = "${var.app_name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  tags = { Name = "${var.app_name}-alb" }
}

resource "aws_lb_target_group" "app" {
  for_each = var.environments

  name        = "${var.app_name}-${each.key}-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    path    = "/"
    # 200 = healthy, 401/403 = Spring Security is up but blocking (still healthy)
    matcher             = "200,401,403"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 30
    timeout             = 5
  }

  tags = {
    Name        = "${var.app_name}-${each.key}-tg"
    Environment = each.key
  }
}

# HTTP listener — default action returns 404; host-based rules below route traffic.
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

# Route by hostname: set DNS A records for each domain_name to the ALB DNS.
resource "aws_lb_listener_rule" "app_http" {
  for_each = var.environments

  listener_arn = aws_lb_listener.http.arn
  priority     = each.value.alb_priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app[each.key].arn
  }

  condition {
    host_header {
      values = [var.domain_names[each.key]]
    }
  }
}

# HTTPS listener — only created when acm_certificate_arn is provided.
resource "aws_lb_listener" "https" {
  count = var.acm_certificate_arn != "" ? 1 : 0

  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.acm_certificate_arn

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

resource "aws_lb_listener_rule" "app_https" {
  for_each = var.acm_certificate_arn != "" ? var.environments : {}

  listener_arn = aws_lb_listener.https[0].arn
  priority     = each.value.alb_priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app[each.key].arn
  }

  condition {
    host_header {
      values = [var.domain_names[each.key]]
    }
  }
}
