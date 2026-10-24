# DSY1107 · Evaluación 2 · Pedidos360

Proyecto Cloud Native desarrollado con Angular, Spring Boot, MySQL, Amazon Cognito y RabbitMQ.

## Componentes

- `frontend-angular`: frontend Angular con autenticación Cognito.
- `backend`: API principal de pedidos.
- `notifications-service`: consumidor RabbitMQ con ACK, reintentos, NACK y DLQ.
- `rabbitmq-admin`: administración de colas, exchanges y bindings.
- `terraform`: infraestructura AWS.

## Roles

SOLICITANTE:
- Crear pedidos.
- Editar pedidos pendientes.
- Eliminar pedidos pendientes.

APROBADOR:
- Aprobar pedidos.
- Rechazar pedidos.
- Agregar comentarios.

## RabbitMQ

Se utilizan:

- `pedidos.exchange`
- `pedidos.events.queue`
- `pedidos.dlx`
- `pedidos.dlq`

Los mensajes con error se intentan procesar 3 veces y luego se envían a la DLQ.

## Ejecución

Backend:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

Frontend:
cd frontend-angular
ng serve --port 5173

Notifications:
cd notifications-service
mvn spring-boot:run

RabbitMQ Admin:
cd rabbitmq-admin
mvn spring-boot:run