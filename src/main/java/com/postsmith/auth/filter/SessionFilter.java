package com.postsmith.auth.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.postsmith.auth.service.RedisService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SessionFilter implements GlobalFilter {
    RedisService redisService;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String host = exchange.getRequest().getHeaders().getFirst("Host");
		
		if(!exchange.getRequest().getURI().getPath().startsWith("/api")) {
			return chain.filter(exchange);
		}
		
		System.out.println("1");
		if (host == null || host.isEmpty()) {
			System.out.println("2");
			log.warn("Host header is missing");
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return response.setComplete();
		}

		String sessionId = null;
		if (!exchange.getRequest().getCookies().containsKey("SESSION_ID")) {
			System.out.println("3");
			log.warn("SESSION_ID is not present in cookies");
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}
		System.out.println("4");
		sessionId = exchange.getRequest().getCookies().getFirst("SESSION_ID").getValue();

		return redisService.getValue(sessionId).flatMap(sessionObject -> {
			if (sessionObject == null) {
				System.out.println("5");
				log.warn("Session is not exist");
				ServerHttpResponse response = exchange.getResponse();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				return response.setComplete();
			}

			System.out.println("6");
			ServerHttpRequest request = exchange.getRequest();
			return chain.filter(exchange.mutate().request(request).build());
		}).onErrorResume(error -> {
			System.out.println("7");
			log.error("Error retrieving session information: {}", error.getMessage());
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response.setComplete();
		});
	}

}