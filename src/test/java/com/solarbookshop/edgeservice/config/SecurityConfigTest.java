package com.solarbookshop.edgeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest()
@Import(SecurityConfig.class)
class SecurityConfigTest {
  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  ReactiveClientRegistrationRepository clientRegistrationRepository;

  @Test
  void whenLogoutNotAuthenticatedAndNoCsrfTokenThen403() {
    webTestClient
        .post()
        .uri("/logout")
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void whenLogoutAuthenticatedAndNoCsrfTokenThen403() {
    webTestClient
        .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
        .post()
        .uri("/logout")
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void whenLogoutAuthenticatedAndWithCsrfTokenThen302() {
    when(clientRegistrationRepository.findByRegistrationId("test"))
        .thenReturn(Mono.just(testClientRegistration()));

    webTestClient
        .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
        .mutateWith(SecurityMockServerConfigurers.csrf())
        .post()
        .uri("/logout")
        .exchange()
        .expectStatus().isFound();
  }

  private ClientRegistration testClientRegistration() {
    return ClientRegistration.withRegistrationId("test")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .clientId("test")
        .authorizationUri("https://sso.solarbookshop.com/auth")
        .tokenUri("https://sso.solarbookshop.com/token")
        .redirectUri("https://solarbookshop.com")
        .build();
  }
}