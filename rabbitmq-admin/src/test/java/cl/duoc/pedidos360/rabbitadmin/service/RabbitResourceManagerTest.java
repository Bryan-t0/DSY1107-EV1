package cl.duoc.pedidos360.rabbitadmin.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
//clase para usar rabbitadmin
class RabbitResourceManagerTest {

  private RabbitAdmin rabbitAdmin;

  private RabbitResourceManager manager;

  @BeforeEach
  void setUp() {

    rabbitAdmin =
        org.mockito.Mockito.mock(
            RabbitAdmin.class
        );

    manager =
        new RabbitResourceManager(
            rabbitAdmin
        );
  }

  @Test
  void creaCola() {

    manager.createQueue(
        "cola.prueba"
    );

    verify(rabbitAdmin)
        .declareQueue(
            any(Queue.class)
        );
  }

  @Test
  void eliminaCola() {

    manager.deleteQueue(
        "cola.prueba"
    );

    verify(rabbitAdmin)
        .deleteQueue(
            "cola.prueba"
        );
  }

  @Test
  void creaExchange() {

    manager.createExchange(
        "exchange.prueba"
    );

    verify(rabbitAdmin)
        .declareExchange(
            any(DirectExchange.class)
        );
  }

  @Test
  void creaBinding() {

    manager.createBinding(
        "cola.prueba",
        "exchange.prueba",
        "routing.prueba"
    );

    verify(rabbitAdmin)
        .declareBinding(
            any(Binding.class)
        );
  }
}
