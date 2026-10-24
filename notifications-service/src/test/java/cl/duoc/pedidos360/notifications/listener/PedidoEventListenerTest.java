package cl.duoc.pedidos360.notifications.listener;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rabbitmq.client.Channel;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class PedidoEventListenerTest {

  @Test
  void procesaMensajeCorrectoYConfirmaAck()
      throws Exception {

    PedidoEventListener listener =
        new PedidoEventListener();

    Channel channel =
        org.mockito.Mockito.mock(
            Channel.class
        );

    MessageProperties properties =
        new MessageProperties();

    properties.setDeliveryTag(10L);

    Message message =
        new Message(
            new byte[0],
            properties
        );

    listener.recibir(
        "PEDIDO_CREADO|id=1|estado=PENDIENTE",
        message,
        channel
    );

    verify(channel)
        .basicAck(
            10L,
            false
        );

    verify(channel, never())
        .basicNack(
            10L,
            false,
            false
        );
  }

  @Test
  void mensajeErrorTerminaEnNack()
      throws Exception {

    PedidoEventListener listener =
        new PedidoEventListener();

    Channel channel =
        org.mockito.Mockito.mock(
            Channel.class
        );

    MessageProperties properties =
        new MessageProperties();

    properties.setDeliveryTag(20L);

    Message message =
        new Message(
            new byte[0],
            properties
        );

    listener.recibir(
        "ERROR|prueba",
        message,
        channel
    );

    verify(channel, never())
        .basicAck(
            20L,
            false
        );

    verify(channel)
        .basicNack(
            20L,
            false,
            false
        );
  }
}