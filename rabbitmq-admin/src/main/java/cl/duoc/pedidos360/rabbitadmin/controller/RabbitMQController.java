package cl.duoc.pedidos360.rabbitadmin.controller;

import java.util.Properties;

import cl.duoc.pedidos360.rabbitadmin.service.RabbitResourceManager;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMQController {

  private final RabbitResourceManager manager;

  public RabbitMQController(
      RabbitResourceManager manager) {

    this.manager = manager;
  }

  @PostMapping("/queues")
  public ResponseEntity<?> createQueue(
      @RequestParam String queueName) {

    if (isInvalid(queueName)) {
      return ResponseEntity
          .badRequest()
          .body("El nombre de la cola es obligatorio");
    }

    manager.createQueue(
        queueName.trim()
    );

    return ResponseEntity
        .status(201)
        .body("Cola creada exitosamente");
  }

  @DeleteMapping("/queues/{queueName}")
  public ResponseEntity<?> deleteQueue(
      @PathVariable String queueName) {

    if (isInvalid(queueName)) {
      return ResponseEntity
          .badRequest()
          .body("El nombre de la cola es obligatorio");
    }

    manager.deleteQueue(
        queueName.trim()
    );

    return ResponseEntity.ok(
        "Cola eliminada exitosamente"
    );
  }

  @PostMapping("/exchanges")
  public ResponseEntity<?> createExchange(
      @RequestParam String exchangeName) {

    if (isInvalid(exchangeName)) {
      return ResponseEntity
          .badRequest()
          .body("El nombre del exchange es obligatorio");
    }

    manager.createExchange(
        exchangeName.trim()
    );

    return ResponseEntity
        .status(201)
        .body("Exchange creado exitosamente");
  }

  @DeleteMapping("/exchanges/{exchangeName}")
  public ResponseEntity<?> deleteExchange(
      @PathVariable String exchangeName) {

    if (isInvalid(exchangeName)) {
      return ResponseEntity
          .badRequest()
          .body("El nombre del exchange es obligatorio");
    }

    manager.deleteExchange(
        exchangeName.trim()
    );

    return ResponseEntity.ok(
        "Exchange eliminado exitosamente"
    );
  }

  @PostMapping("/bindings")
  public ResponseEntity<?> createBinding(
      @RequestParam String queueName,
      @RequestParam String exchangeName,
      @RequestParam String routingKey) {

    if (isInvalid(queueName)
        || isInvalid(exchangeName)
        || isInvalid(routingKey)) {

      return ResponseEntity
          .badRequest()
          .body(
              "Queue, exchange y routingKey son obligatorios"
          );
    }

    manager.createBinding(
        queueName.trim(),
        exchangeName.trim(),
        routingKey.trim()
    );

    return ResponseEntity
        .status(201)
        .body("Binding creado exitosamente");
  }

  @DeleteMapping("/bindings")
  public ResponseEntity<?> deleteBinding(
      @RequestParam String queueName,
      @RequestParam String exchangeName,
      @RequestParam String routingKey) {

    if (isInvalid(queueName)
        || isInvalid(exchangeName)
        || isInvalid(routingKey)) {

      return ResponseEntity
          .badRequest()
          .body(
              "Queue, exchange y routingKey son obligatorios"
          );
    }

    manager.deleteBinding(
        queueName.trim(),
        exchangeName.trim(),
        routingKey.trim()
    );

    return ResponseEntity.ok(
        "Binding eliminado exitosamente"
    );
  }

  @GetMapping("/queues/{queueName}")
  public ResponseEntity<?> getQueueInfo(
      @PathVariable String queueName) {

    if (isInvalid(queueName)) {
      return ResponseEntity
          .badRequest()
          .body("El nombre de la cola es obligatorio");
    }

    Properties info =
        manager.getQueueInfo(
            queueName.trim()
        );

    if (info == null) {
      return ResponseEntity
          .notFound()
          .build();
    }

    return ResponseEntity.ok(info);
  }

  @DeleteMapping("/queues/{queueName}/messages")
  public ResponseEntity<?> purgeQueue(
      @PathVariable String queueName) {

    if (isInvalid(queueName)) {
      return ResponseEntity
          .badRequest()
          .body("El nombre de la cola es obligatorio");
    }

    manager.purgeQueue(
        queueName.trim()
    );

    return ResponseEntity.ok(
        "Mensajes eliminados de la cola"
    );
  }

  private boolean isInvalid(String value) {

    return value == null
        || value.trim().isEmpty();
  }
}