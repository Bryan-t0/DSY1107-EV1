package cl.duoc.pedidos360.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${app.jwt.issuer}")
  private String issuer;

  @Value("${app.jwt.client-id}")
  private String clientId;

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .authorizeHttpRequests(auth -> auth
        // dejo pasar OPTIONS porque el navegador lo usa para CORS
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        // esta ruta queda publica para revisar si el backend esta vivo
        .requestMatchers("/actuator/health").permitAll()
        // esta ruta necesita ser APROBADOR
        .requestMatchers("/datos").hasRole("APROBADOR")
        // para entrar a pedidos el usuario tiene que estar autenticado
        .requestMatchers("/pedidos/**").authenticated()
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth -> oauth
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
      );

    return http.build();
  }

  @Bean
  // configuracion de CORS del backend
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // frontend que tiene permiso para llamar al backend
    configuration.setAllowedOrigins(
      List.of("http://localhost:5173")
    );

    // metodos que puede usar el frontend
    configuration.setAllowedMethods(
      List.of("GET", "POST", "PUT", "OPTIONS")
    );

    // headers que permitimos desde el frontend
    configuration.setAllowedHeaders(
      List.of("Authorization", "Content-Type")
    );

    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  @Bean
  Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopeConverter =
      new JwtGrantedAuthoritiesConverter();

    return jwt -> {
      Collection<GrantedAuthority> authorities =
        new ArrayList<>(scopeConverter.convert(jwt));

      // saco los grupos que vienen dentro del token de Cognito
      List<String> groups =
        jwt.getClaimAsStringList("cognito:groups");

      if (groups != null) {
        groups.forEach(group ->
          authorities.add(
            // convierto el grupo de Cognito a un rol que entiende Spring
            new SimpleGrantedAuthority("ROLE_" + group)
          )
        );
      }

      return new JwtAuthenticationToken(jwt, authorities);
    };
  }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder =
      (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);

    // revisa que el token venga del issuer correcto
    OAuth2TokenValidator<Jwt> defaults =
      JwtValidators.createDefaultWithIssuer(issuer);

    // revisa que el token pertenezca a nuestro cliente
    OAuth2TokenValidator<Jwt> clientValidator =
      new JwtClaimValidator<>(
        "client_id",
        claim -> clientId.equals(String.valueOf(claim))
      );

    // si viene aud, reviso que incluya nuestra aplicacion
    OAuth2TokenValidator<Jwt> audienceValidator =
      new JwtClaimValidator<List<String>>(
        "aud",
        aud -> aud == null || aud.isEmpty() || aud.contains(clientId)
      );

    decoder.setJwtValidator(
      new DelegatingOAuth2TokenValidator<>(
        defaults,
        clientValidator,
        audienceValidator
      )
    );

    return decoder;
  }
}