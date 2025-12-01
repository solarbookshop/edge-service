package com.solarbookshop.edgeservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class WebEndpoints {
  @Bean
  public RouterFunction<ServerResponse> routes() {
    return RouterFunctions.route()
            .GET("/catalog-fallback", _ ->
                    ServerResponse.ok().body(Mono.just("Catalog fallback"), String.class))
            .POST("/catalog-fallback", _ ->
                    ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build())
            .build();
  }
}
