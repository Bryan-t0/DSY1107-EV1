variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "dsy1107-pedidos360"
}

variable "cognito_domain_prefix" {
  type    = string
  default = "dsy1107-bryant-001"
}

variable "frontend_callback_url" {
  type    = string
  default = "http://localhost:5173/"
}

variable "backend_integration_uri" {
  type        = string
  default     = "http://example.com:8080/datos"
  description = "URI inicial del backend; el pipeline backend_deploy la reapunta a la task ECS."
}
