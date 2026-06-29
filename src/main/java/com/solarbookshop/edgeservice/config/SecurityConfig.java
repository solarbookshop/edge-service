package com.solarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {
  @Bean
  SecurityWebFilterChain filterChain(
      ServerHttpSecurity http,
      ReactiveClientRegistrationRepository clientRegistrationRepository) {
    return http
        .authorizeExchange(exchangeSpec -> exchangeSpec
            .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
            .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
            .anyExchange().authenticated())
        .exceptionHandling(customizer -> customizer
            .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
        .oauth2Login(Customizer.withDefaults())
        .logout(logoutSpec -> logoutSpec
            .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
        .csrf(csrfSpec -> csrfSpec
            .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler()))
        .build();
  }

  private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository repository) {
    var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(repository);
    oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
    return oidcLogoutSuccessHandler;
  }

  @Bean
  WebFilter csrfWebFilter() {
//    Required because of https://github.com/spring-projects/spring-security/issues/5766
//    CookieServerCsrfTokenRepository doesn’t ensure a subscription to CsrfToken,
//    this explicitly provides a workaround in a WebFilter bean.
    return (exchange, chain) -> {
      exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
        Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.then() : Mono.empty();
      }));
      return chain.filter(exchange);
    };
  }

  @Bean
  ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
//    This implementation that stores Access Tokens in the web session. Spring Session will pick
//    them up automatically and save them in Redis, just like it does with ID Tokens.
//    The default implementation for this repository adopts an in-memory strategy
//    for persistence, which makes edge-service stateful.
    return new WebSessionServerOAuth2AuthorizedClientRepository();
  }
}
