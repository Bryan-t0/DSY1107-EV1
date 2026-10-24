package cl.duoc.pedidos360.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = false)
@Profile("local")
public class LocalSecurityConfig {

  @Bean
  SecurityFilterChain localSecurity(
      HttpSecurity http) throws Exception {

    http
      .csrf(csrf -> csrf.disable())
      .cors(cors ->
        cors.configurationSource(
          localCorsConfigurationSource()
        )
      )
      .authorizeHttpRequests(auth ->
        auth.anyRequest().permitAll()
      );

    return http.build();
  }

  @Bean
  CorsConfigurationSource localCorsConfigurationSource() {

    CorsConfiguration configuration =
        new CorsConfiguration();

    configuration.setAllowedOrigins(
        List.of(
            "http://localhost:4200",
            "http://localhost:5173"
        )
    );

    configuration.setAllowedMethods(
        List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
        )
    );

    configuration.setAllowedHeaders(
        List.of(
            "Authorization",
            "Content-Type"
        )
    );

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration(
        "/**",
        configuration
    );

    return source;
  }
}