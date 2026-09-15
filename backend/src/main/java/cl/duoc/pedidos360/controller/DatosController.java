package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.repository.PedidoRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatosController {

  private final PedidoRepository pedidoRepository;

  public DatosController(PedidoRepository pedidoRepository) {
    this.pedidoRepository = pedidoRepository;
  }

  @GetMapping("/datos")
  public Map<String, Object> datos(@AuthenticationPrincipal Jwt jwt) {
    Map<String, Object> respuesta = new LinkedHashMap<>();

    respuesta.put("mensaje", "Acceso autorizado a Pedidos360");
    respuesta.put("fecha", Instant.now().toString());
    respuesta.put("subject", jwt.getSubject());
    respuesta.put("username", jwt.getClaimAsString("username"));
    respuesta.put("client_id", jwt.getClaimAsString("client_id"));
    respuesta.put("scope", jwt.getClaimAsString("scope"));
    respuesta.put("grupos", jwt.getClaim("cognito:groups"));
    respuesta.put("pedidos_en_bd", pedidoRepository.count());

    return respuesta;
  }
}
