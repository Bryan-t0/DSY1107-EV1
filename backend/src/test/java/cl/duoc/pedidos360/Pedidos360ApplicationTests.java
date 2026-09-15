package cl.duoc.pedidos360;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
  "app.jwt.issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ZzHU4WlkI",
  "app.jwt.client-id=6t8gda2gsqh5h61jk5rgclc55s",
  "spring.datasource.url=jdbc:h2:mem:testdb",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.datasource.username=sa",
  "spring.datasource.password=",
  "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
  "spring.jpa.hibernate.ddl-auto=create-drop"
})
class Pedidos360ApplicationTests {

  @MockitoBean
  JwtDecoder jwtDecoder;

  @Test
  void contextLoads() {
  }
}