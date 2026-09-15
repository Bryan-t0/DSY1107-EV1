resource "aws_cognito_user_pool" "pool" {
  name = var.project_name
  username_attributes = ["email"]
  auto_verified_attributes = ["email"]
  password_policy { minimum_length = 8 require_lowercase = true require_uppercase = true require_numbers = true require_symbols = false }
}

resource "aws_cognito_user_pool_domain" "hosted_ui" {
  domain = var.cognito_domain_prefix
  user_pool_id = aws_cognito_user_pool.pool.id
  managed_login_version = 1
}

resource "aws_cognito_user_pool_client" "spa" {
  name = "spa-react"
  user_pool_id = aws_cognito_user_pool.pool.id
  generate_secret = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows = ["code"]
  supported_identity_providers = ["COGNITO"]
  allowed_oauth_scopes = ["openid", "email", "profile"]
  callback_urls = [var.frontend_callback_url]
  logout_urls = [var.frontend_callback_url]
  explicit_auth_flows = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
  access_token_validity = 60
  id_token_validity = 60
  refresh_token_validity = 30
  token_validity_units { access_token = "minutes" id_token = "minutes" refresh_token = "days" }
}
