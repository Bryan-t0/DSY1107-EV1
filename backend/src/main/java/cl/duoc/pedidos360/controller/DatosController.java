package cl.duoc.pedidos360.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatosController {
  @GetMapping("/datos")
  public Map<String,Object> datos(@AuthenticationPrincipal Jwt jwt) {
    Map<String,Object> r = new LinkedHashMap<>();
    r.put("mensaje", "Acceso autorizado a Pedidos360");
    r.put("fecha", Instant.now().toString());
    r.put("subject", jwt.getSubject());
    r.put("username", jwt.getClaimAsString("username"));
    r.put("client_id", jwt.getClaimAsString("client_id"));
    r.put("scope", jwt.getClaimAsString("scope"));
    r.put("grupos", jwt.getClaim("cognito:groups"));
    return r;
  }
}
