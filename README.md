# DSY1107 · Evaluación Parcial 1 · Pedidos360

Solución Cloud Native con React, AWS Cognito (OIDC Authorization Code + PKCE), API Gateway con JWT Authorizer, backend Spring Boot protegido como Resource Server, contenedor Docker en ECR/ECS Fargate y frontend en AWS Amplify.

## Estructura
- `frontend/`: React + Vite, login/logout, PKCE, refresh token, lectura de claims y consumo del API con `Authorization: Bearer`.
- `backend/`: Spring Boot 3 / Java 21, endpoint `GET /datos`, validación de firma, issuer, expiración y `client_id`/audience.
- `terraform/`: Cognito, ECR, ECS Fargate, API Gateway JWT y Amplify.
- `.github/workflows/`: 4 pipelines obligatorios (compile/deploy frontend y backend).

## Despliegue de infraestructura
```bash
cd terraform
terraform init
terraform validate
terraform plan
terraform apply
terraform output
```

## Variables GitHub Actions
Crear en **Settings → Secrets and variables → Actions → Variables**:
`AWS_REGION`, `COGNITO_DOMAIN`, `COGNITO_CLIENT_ID`, `REDIRECT_URI`, `API_URL`, `ECR_REPO`, `ECS_CLUSTER`, `ECS_SERVICE`, `API_ID`, `INTEGRATION_ID`, `AMPLIFY_APP_ID`, `AMPLIFY_BRANCH`.

Los valores salen de `terraform output`. `REDIRECT_URI` debe coincidir exactamente con la callback de Cognito.

## Secrets GitHub Actions
Crear `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN` usando las credenciales temporales del Learner Lab. Deben renovarse cuando expire/reinicie la sesión del laboratorio.

## Desarrollo local
Frontend:
```bash
cd frontend
npm install
npm run dev
```
Backend (requiere valores Cognito reales):
```bash
cd backend
COGNITO_ISSUER=https://cognito-idp.us-east-1.amazonaws.com/POOL_ID COGNITO_CLIENT_ID=CLIENT_ID mvn spring-boot:run
```

## Flujo de seguridad
1. React inicia Authorization Code + PKCE contra Cognito.
2. Cognito entrega access/id/refresh token.
3. React envía `Authorization: Bearer <access_token>`.
4. API Gateway valida JWT con Cognito.
5. Spring Security vuelve a validar firma, issuer, vigencia y client ID.
6. `/datos` devuelve información solo a usuarios autenticados.
