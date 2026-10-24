package cl.duoc.pedidos360.messaging;

import cl.duoc.pedidos360.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedidoEventProducer {

  private static final Logger log =
      LoggerFactory.getLogger(PedidoEventProducer.class);

  private final RabbitTemplate rabbitTemplate;

  public PedidoEventProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publicar(String tipo, Pedido pedido) {

    String mensaje =
        tipo
        + "|id=" + pedido.getId()
        + "|descripcion=" + pedido.getDescripcion()
        + "|estado=" + pedido.getEstado();

    rabbitTemplate.convertAndSend(
        RabbitMessagingConfig.PEDIDOS_EXCHANGE,
        RabbitMessagingConfig.PEDIDOS_ROUTING_KEY,
        mensaje
    );

    log.info(
        "Evento RabbitMQ publicado: {}",
        mensaje
    );
  }
}