package cl.duoc.pedidos360;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {"app.jwt.issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ZzHU4WlkI","app.jwt.client-id=6t8gda2gsqh5h61jk5rgclc55s"})
class Pedidos360ApplicationTests {
  @MockitoBean
  JwtDecoder jwtDecoder;

  @Test
  void contextLoads() {}
}
