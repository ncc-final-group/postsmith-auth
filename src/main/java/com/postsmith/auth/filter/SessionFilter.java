package com.postsmith.auth.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.postsmith.auth.dto.UserSessionDto;
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
		System.out.println("host = " + host);
		String subdomain = "";

		String sessionId = null;
		if (exchange.getRequest().getCookies().containsKey("SESSION_ID")) {
			try {
				sessionId = exchange.getRequest().getCookies().getFirst("SESSION_ID").getValue();
			} catch (NullPointerException e) {
				log.warn("SESSION_ID 쿠키가 존재하지 않습니다: ", e);
			}
		}

		if (sessionId == null) {
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "세션이 존재하지 않습니다."));
		}

		return redisService.findById(sessionId).flatMap(userSession -> {
			if (userSession == null || userSession.getUserId() == null) {
				log.warn("세션에 user 값이 존재하지 않습니다: sessionId={}", userSession.getSessionId());
				return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user 값이 존재하지 않습니다."));
			}
			String user = userSession.getUserId();
			if (host != null && host.contains(".localhost")) { // subdomain 이 존재하는 경우
				String subdomain1 = host.substring(0, host.indexOf(".localhost"));
				ServerHttpRequest request = exchange.getRequest().mutate().header("subdomain", subdomain1).header("user", user).build();
				return chain.filter(exchange.mutate().request(request).build());
			} else { // subdomain 이 존재하지 않는 경우
				ServerHttpRequest request = exchange.getRequest().mutate().header("user", user).build();
				return chain.filter(exchange.mutate().request(request).build());
			}
		}).onErrorResume(error -> {
			log.error("Redis에서 세션 정보를 가져오는 중 오류 발생: ", error);
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "세션 정보를 가져오는 중 오류가 발생했습니다."));
		});
	}

}