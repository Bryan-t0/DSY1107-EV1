resource "aws_amplify_app" "frontend" {
  name     = "${var.project_name}-frontend"
  platform = "WEB"
}

resource "aws_amplify_branch" "main" {
  app_id      = aws_amplify_app.frontend.id
  branch_name = "main"
  stage       = "PRODUCTION"
}
