package cl.duoc.pedidos360;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.jwt.issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_example","app.jwt.client-id=test-client"})
class Pedidos360ApplicationTests {
  @Test void contextLoads() {}
}
