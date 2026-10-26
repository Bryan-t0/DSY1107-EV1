package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.messaging.PedidoEventProducer;
import cl.duoc.pedidos360.model.Pedido;
import cl.duoc.pedidos360.repository.PedidoRepository;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

//Peticiones REST de los pedidos
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

  private final PedidoRepository pedidoRepository;
  private final PedidoEventProducer eventProducer;

  public PedidoController(
      PedidoRepository pedidoRepository,
      PedidoEventProducer eventProducer) {

    this.pedidoRepository = pedidoRepository;
    this.eventProducer = eventProducer;
  }

  @GetMapping
  public List<Pedido> listar() {
    return pedidoRepository.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Pedido> obtenerPorId(
      @PathVariable Long id) {

    return pedidoRepository.findById(id)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('SOLICITANTE','APROBADOR')")
  public ResponseEntity<?> crear(
      @RequestBody Map<String, String> body) {

    String descripcion =
        body.getOrDefault("descripcion", "").trim();

    if (descripcion.isBlank()) {
      return ResponseEntity
        .badRequest()
        .body("La descripcion es obligatoria");
    }

    Pedido pedido = new Pedido(descripcion);

    Pedido guardado =
        pedidoRepository.save(pedido);

    eventProducer.publicar(
        "PEDIDO_CREADO",
        guardado
    );

    return ResponseEntity.ok(guardado);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SOLICITANTE')")
  public ResponseEntity<?> editar(
      @PathVariable Long id,
      @RequestBody Map<String, String> body) {

    String descripcion =
        body.getOrDefault("descripcion", "").trim();

    if (descripcion.isBlank()) {
      return ResponseEntity
        .badRequest()
        .body("La descripcion es obligatoria");
    }

    return pedidoRepository.findById(id)
      .map(pedido -> {

        if (!"PENDIENTE".equals(pedido.getEstado())) {
          return ResponseEntity
            .badRequest()
            .body("Solo se pueden editar pedidos pendientes");
        }

        pedido.setDescripcion(descripcion);

        Pedido guardado =
            pedidoRepository.save(pedido);

        eventProducer.publicar(
            "PEDIDO_EDITADO",
            guardado
        );

        return ResponseEntity.ok(guardado);
      })
      .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SOLICITANTE')")
  public ResponseEntity<?> eliminar(
      @PathVariable Long id) {

    return pedidoRepository.findById(id)
      .map(pedido -> {

        if (!"PENDIENTE".equals(pedido.getEstado())) {
          return ResponseEntity
            .badRequest()
            .body("Solo se pueden eliminar pedidos pendientes");
        }

        eventProducer.publicar(
            "PEDIDO_ELIMINADO",
            pedido
        );

        pedidoRepository.delete(pedido);

        return ResponseEntity.ok(
            "Pedido eliminado correctamente"
        );
      })
      .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/aprobar")
  @PreAuthorize("hasRole('APROBADOR')")
  public ResponseEntity<Pedido> aprobar(
      @PathVariable Long id,
      @RequestBody(required = false)
      Map<String, String> body) {

    return pedidoRepository.findById(id)
      .map(pedido -> {

        pedido.setEstado("APROBADO");

        if (body != null) {
          String comentario =
              body.getOrDefault("comentario", "").trim();

          pedido.setComentario(comentario);
        }

        Pedido guardado =
            pedidoRepository.save(pedido);

        eventProducer.publicar(
            "PEDIDO_APROBADO",
            guardado
        );

        return ResponseEntity.ok(guardado);
      })
      .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/rechazar")
  @PreAuthorize("hasRole('APROBADOR')")
  public ResponseEntity<?> rechazar(
      @PathVariable Long id,
      @RequestBody Map<String, String> body) {

    String comentario =
        body.getOrDefault("comentario", "").trim();

    if (comentario.isBlank()) {
      return ResponseEntity
        .badRequest()
        .body("El comentario es obligatorio para rechazar");
    }

    return pedidoRepository.findById(id)
      .map(pedido -> {

        pedido.setEstado("RECHAZADO");
        pedido.setComentario(comentario);

        Pedido guardado =
            pedidoRepository.save(pedido);

        eventProducer.publicar(
            "PEDIDO_RECHAZADO",
            guardado
        );

        return ResponseEntity.ok(guardado);
      })
      .orElse(ResponseEntity.notFound().build());
  }
}
