resource "aws_apigatewayv2_api" "http" {
  name          = "${var.project_name}-api"
  protocol_type = "HTTP"

  // permite que el frontend pueda comunicarse con la API
  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "PUT", "OPTIONS"]
    allow_headers = ["authorization", "content-type"]
  }
}

// valida el token antes de dejar pasar a la API
resource "aws_apigatewayv2_authorizer" "cognito" {
  api_id           = aws_apigatewayv2_api.http.id
  // este autorizador usa validacion por JWT
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  name             = "cognito-jwt"

  jwt_configuration {
    // revisa que el token sea para nuestra aplicacion
    audience = [aws_cognito_user_pool_client.spa.id]
    // revisa que el token venga de Cognito
    issuer   = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.pool.id}"
  }
}

// conecta API Gateway con el backend
resource "aws_apigatewayv2_integration" "datos" {
  api_id                 = aws_apigatewayv2_api.http.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "GET"
  integration_uri        = var.backend_integration_uri
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "datos" {
  api_id             = aws_apigatewayv2_api.http.id
  // define el metodo y la ruta de la API
  route_key          = "GET /datos"
  target             = "integrations/${aws_apigatewayv2_integration.datos.id}"
  // esta ruta necesita un token valido
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.cognito.id
}

// conecta API Gateway con el backend
resource "aws_apigatewayv2_integration" "pedidos" {
  api_id                 = aws_apigatewayv2_api.http.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = replace(var.backend_integration_uri, "/datos", "")
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "pedidos" {
  api_id             = aws_apigatewayv2_api.http.id
  // define el metodo y la ruta de la API
  route_key          = "ANY /pedidos"
  target             = "integrations/${aws_apigatewayv2_integration.pedidos.id}"
  // esta ruta necesita un token valido
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.cognito.id
}

resource "aws_apigatewayv2_route" "pedidos_proxy" {
  api_id             = aws_apigatewayv2_api.http.id
  // define el metodo y la ruta de la API
  route_key          = "ANY /pedidos/{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.pedidos.id}"
  // esta ruta necesita un token valido
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.cognito.id
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http.id
  name        = "$default"
  auto_deploy = true
}