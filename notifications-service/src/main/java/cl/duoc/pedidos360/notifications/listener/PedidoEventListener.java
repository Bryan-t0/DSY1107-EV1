package cl.duoc.pedidos360.notifications.listener;

import cl.duoc.pedidos360.notifications.config.RabbitMessagingConfig;
import com.rabbitmq.client.Channel;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

//CONSUMIDOR
@Component
public class PedidoEventListener {

  private static final Logger log =
      LoggerFactory.getLogger(PedidoEventListener.class);

  private static final int MAX_INTENTOS = 3;
//El método está escuchando pedidos.events.queue, por eso este es nuestro consumidor
  @RabbitListener(
      queues = RabbitMessagingConfig.PEDIDOS_QUEUE,
      containerFactory = "rabbitListenerContainerFactory"
  )
  public void recibir(
      String mensaje,
      Message message,
      Channel channel) throws IOException {

    long deliveryTag =
        message.getMessageProperties().getDeliveryTag();

    for (int intento = 1;
         intento <= MAX_INTENTOS;
         intento++) {

      try {

        log.info(
            "Procesando mensaje. Intento {}/{}: {}",
            intento,
            MAX_INTENTOS,
            mensaje
        );

        procesarMensaje(mensaje);

        channel.basicAck(
            deliveryTag,
            false
        );

        log.info(
            "Mensaje procesado correctamente: {}",
            mensaje
        );

        return;

      } catch (Exception e) {

        log.error(
            "Error procesando mensaje en intento {}: {}",
            intento,
            e.getMessage()
        );

        if (intento < MAX_INTENTOS) {
          esperarAntesDeReintentar(intento);
        }
      }
    }

    log.error(
        "Mensaje rechazado despues de {} intentos. Se enviara a DLQ: {}",
        MAX_INTENTOS,
        mensaje
    );

    channel.basicNack(
        deliveryTag,
        false,
        false
    );
  }

  private void procesarMensaje(String mensaje) {

    if (mensaje == null || mensaje.isBlank()) {
      throw new IllegalArgumentException(
          "El mensaje esta vacio"
      );
    }

    /*
     * Se utiliza ERROR para probar de forma controlada
     * el flujo de reintentos y Dead Letter Queue.
     */
    if (mensaje.toUpperCase().contains("ERROR")) {
      throw new IllegalStateException(
          "Error simulado para probar DLQ"
      );
    }

    log.info(
        "Notificacion generada para evento: {}",
        mensaje
    );
  }

  private void esperarAntesDeReintentar(
      int intento) {

    try {

      long espera =
          intento * 1000L;

      Thread.sleep(espera);

    } catch (InterruptedException e) {

      Thread.currentThread().interrupt();

      throw new IllegalStateException(
          "Proceso de reintento interrumpido",
          e
      );
    }
  }
}
