package com.postsmith.auth.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.postsmith.auth.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionFilter implements GlobalFilter, Ordered {
	private final RedisService redisService;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String host = exchange.getRequest().getHeaders().getFirst("Host");
		
		List<String> allowedUris = List.of("/oauth2", "/login", "/logout", "/api");
		if (allowedUris.stream().anyMatch(uri -> exchange.getRequest().getURI().getPath().startsWith(uri))) {
			return chain.filter(exchange);
		}

		if (host == null || host.isEmpty()) {
			log.warn("Host header is missing");
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return response.setComplete();
		}

		String sessionId = null;
		if (!exchange.getRequest().getCookies().containsKey("CLIENT_SESSION_ID")) {
			log.warn("CLIENT_SESSION_ID is not present in cookies");
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}

		sessionId = exchange.getRequest().getCookies().getFirst("CLIENT_SESSION_ID").getValue();
		return redisService.getValue(sessionId).flatMap(sessionObject -> {
			if (sessionObject == null) {
				log.warn("Session is not exist");
				ServerHttpResponse response = exchange.getResponse();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				return response.setComplete();
			}

			ServerHttpRequest request = exchange.getRequest();
			return chain.filter(exchange.mutate().request(request).build());
		}).onErrorResume(error -> {
			log.error("Error retrieving session information: {}", error.getMessage());
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response.setComplete();
		});
	}

	@Override
	public int getOrder() {
		return 0;
	}
}