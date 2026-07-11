package com.solarbookshop.edgeservice.config;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  public @NonNull Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    var path = exchange.getRequest().getURI().getPath();
    var method = exchange.getRequest().getMethod().name();

    if (!shouldLog(path)) {
      return chain.filter(exchange);
    }

    return exchange.getPrincipal()
        .map(this::getUsername)
        .defaultIfEmpty("anonymous")
        .doOnNext(user -> log.info("📡 Routing request: user={}, method={}, path={}", user, method, path))
        .then(chain.filter(exchange));
  }

  private String getUsername(Principal principal) {
    if (principal instanceof OAuth2AuthenticationToken token) {
      if (token.getPrincipal() instanceof OidcUser oidcUser) {
        return oidcUser.getPreferredUsername();
      }
      return token.getPrincipal().getName();
    }
    return principal.getName();
  }

  private boolean shouldLog(String path) {
    if (path.startsWith("/actuator/")) {
      return false;
    }
    // Exclude static assets
    return !path.endsWith(".js") && !path.endsWith(".css") && !path.endsWith(".ico") &&
        !path.endsWith(".png") && !path.endsWith(".jpg") && !path.endsWith(".html") &&
        !path.endsWith(".json") && !path.endsWith(".svg") && !path.endsWith(".txt");
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE; // Runs after security filter matches authentication
  }
}
