// donde se manejan los usuarios de Cognito
resource "aws_cognito_user_pool" "pool" {
  name = "dsy1107-grupoxx-001"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = false
  }

  admin_create_user_config {
    allow_admin_create_user_only = true
  }
}

// dominio que usa la pantalla de login de Cognito
resource "aws_cognito_user_pool_domain" "hosted_ui" {
  domain       = "dsy1107-grupoxx-001"
  user_pool_id = aws_cognito_user_pool.pool.id

  # 1 = Hosted UI clásica
  managed_login_version = 1
}
// registra nuestro frontend como aplicacion en Cognito
resource "aws_cognito_user_pool_client" "spa" {
  name         = "spa-react"
  user_pool_id = aws_cognito_user_pool.pool.id

  // React no usa secret porque funciona desde el navegador
  generate_secret = false

  allowed_oauth_flows_user_pool_client = true
  // usamos el flujo Authorization Code
  allowed_oauth_flows                   = ["code"]

  supported_identity_providers = ["COGNITO"]

  // permisos que pide nuestra aplicacion
  allowed_oauth_scopes = ["openid", "email", "profile"]

  // donde Cognito devuelve al usuario despues del login
  callback_urls = ["http://localhost:5173/"]
  // donde vuelve el usuario despues de cerrar sesion
  logout_urls   = ["http://localhost:5173/"]

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]

  access_token_validity = 60
  id_token_validity     = 60

  token_validity_units {
    access_token = "minutes"
    id_token     = "minutes"
  }
}
