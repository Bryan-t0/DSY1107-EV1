package cl.duoc.pedidos360.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {

  public static final String PEDIDOS_EXCHANGE = "pedidos.exchange"; //exchange principal
  public static final String PEDIDOS_QUEUE = "pedidos.events.queue"; // cola principal
  public static final String PEDIDOS_ROUTING_KEY = "pedidos.event"; //routing key, para ayudar a rabbitmq a direccionar las colas

  public static final String PEDIDOS_DLX = "pedidos.dlx";
  public static final String PEDIDOS_DLQ = "pedidos.dlq";
  public static final String PEDIDOS_DLQ_ROUTING_KEY = "pedidos.error";

  @Bean
  public DirectExchange pedidosExchange() {
    return new DirectExchange(PEDIDOS_EXCHANGE, true, false);
  }

  @Bean
  public Queue pedidosQueue() {
    return QueueBuilder
      .durable(PEDIDOS_QUEUE)
      .deadLetterExchange(PEDIDOS_DLX)
      .deadLetterRoutingKey(PEDIDOS_DLQ_ROUTING_KEY)
      .build();
  }

  @Bean
  public Binding pedidosBinding() { //binding conecta exchange con la cola
    return BindingBuilder
      .bind(pedidosQueue())
      .to(pedidosExchange())
      .with(PEDIDOS_ROUTING_KEY);
  }
//////////////////////////////////////////////////DQL//////////////////////////////////////////////////////////
  @Bean
  public DirectExchange pedidosDeadLetterExchange() {
    return new DirectExchange(PEDIDOS_DLX, true, false);
  }

  @Bean
  public Queue pedidosDeadLetterQueue() {
    return QueueBuilder
      .durable(PEDIDOS_DLQ)
      .build();
  }

  @Bean
  public Binding pedidosDeadLetterBinding() {
    return BindingBuilder
      .bind(pedidosDeadLetterQueue())
      .to(pedidosDeadLetterExchange())
      .with(PEDIDOS_DLQ_ROUTING_KEY);
  }
}
