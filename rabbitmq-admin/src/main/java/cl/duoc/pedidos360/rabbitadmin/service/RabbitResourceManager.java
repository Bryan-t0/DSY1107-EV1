package cl.duoc.pedidos360.rabbitadmin.service;

import java.util.Properties;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class RabbitResourceManager {

  private final RabbitAdmin rabbitAdmin;

  public RabbitResourceManager(RabbitAdmin rabbitAdmin) {
    this.rabbitAdmin = rabbitAdmin;
  }

  public void createQueue(String queueName) {

    Queue queue = new Queue(
        queueName,
        true,
        false,
        false
    );

    rabbitAdmin.declareQueue(queue);
  }

  public void deleteQueue(String queueName) {
    rabbitAdmin.deleteQueue(queueName);
  }

  public void createExchange(String exchangeName) {

    DirectExchange exchange =
        new DirectExchange(
            exchangeName,
            true,
            false
        );

    rabbitAdmin.declareExchange(exchange);
  }

  public void deleteExchange(String exchangeName) {
    rabbitAdmin.deleteExchange(exchangeName);
  }

  public void createBinding(
      String queueName,
      String exchangeName,
      String routingKey) {

    Queue queue = new Queue(
        queueName,
        true,
        false,
        false
    );

    DirectExchange exchange =
        new DirectExchange(
            exchangeName,
            true,
            false
        );

    Binding binding =
        BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(routingKey);

    rabbitAdmin.declareBinding(binding);
  }

  public void deleteBinding(
      String queueName,
      String exchangeName,
      String routingKey) {

    Queue queue = new Queue(
        queueName,
        true,
        false,
        false
    );

    DirectExchange exchange =
        new DirectExchange(
            exchangeName,
            true,
            false
        );

    Binding binding =
        BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(routingKey);

    rabbitAdmin.removeBinding(binding);
  }

  public Properties getQueueInfo(
      String queueName) {

    return rabbitAdmin.getQueueProperties(
        queueName
    );
  }

  public void purgeQueue(String queueName) {

    rabbitAdmin.purgeQueue(
        queueName,
        false
    );
  }
}