package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.model.Pedido;
import cl.duoc.pedidos360.repository.PedidoRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

  private final PedidoRepository pedidoRepository;

  public PedidoController(PedidoRepository pedidoRepository) {
    this.pedidoRepository = pedidoRepository;
  }

  // lista los pedidos
  @GetMapping
  public List<Pedido> listar() {
    return pedidoRepository.findAll();
  }

  // crea un pedido nuevo
  @PostMapping
  // estos dos roles pueden crear pedidos
  @PreAuthorize("hasAnyRole('SOLICITANTE','APROBADOR')")
  public Pedido crear(@RequestBody Map<String, String> body) {
    String descripcion = body.getOrDefault("descripcion", "").trim();
    if (descripcion.isBlank()) {
      throw new IllegalArgumentException("La descripcion es obligatoria");
    }
    // guarda el pedido en la base de datos
    return pedidoRepository.save(new Pedido(descripcion));
  }

  // ruta para aprobar un pedido
  @PutMapping("/{id}/aprobar")
  // solo el aprobador puede hacer esta accion
  @PreAuthorize("hasRole('APROBADOR')")
  public ResponseEntity<Pedido> aprobar(@PathVariable Long id) {
    return pedidoRepository.findById(id)
      .map(pedido -> {
        pedido.setEstado("APROBADO");
        // guarda el cambio en la base de datos
        return ResponseEntity.ok(pedidoRepository.save(pedido));
      })
      .orElse(ResponseEntity.notFound().build());
  }

  // ruta para rechazar un pedido
  @PutMapping("/{id}/rechazar")
  // solo el aprobador puede hacer esta accion
  @PreAuthorize("hasRole('APROBADOR')")
  public ResponseEntity<Pedido> rechazar(@PathVariable Long id) {
    return pedidoRepository.findById(id)
      .map(pedido -> {
        pedido.setEstado("RECHAZADO");
        // guarda el cambio en la base de datos
        return ResponseEntity.ok(pedidoRepository.save(pedido));
      })
      .orElse(ResponseEntity.notFound().build());
  }
}
