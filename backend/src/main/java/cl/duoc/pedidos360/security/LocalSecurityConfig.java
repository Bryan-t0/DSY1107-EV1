package cl.duoc.pedidos360.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = false)
@Profile("local")
public class LocalSecurityConfig {

  @Bean
  SecurityFilterChain localSecurity(
      HttpSecurity http) throws Exception {

    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth ->
        auth.anyRequest().permitAll()
      );

    return http.build();
  }
}