package com.solarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EdgeServiceApplicationTests {
  @MockitoBean
  ReactiveClientRegistrationRepository clientRegistrationRepository;

  @Test
  void contextLoads() {
  }
}
