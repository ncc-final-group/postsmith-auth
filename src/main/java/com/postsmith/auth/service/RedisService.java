package com.postsmith.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postsmith.auth.dto.UserSessionDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final ReactiveRedisOperations<String, UserSessionDto> reactiveRedisOps;
	private final ObjectMapper objectMapper;

	// CREATE
	public Mono<Boolean> setValue(String key, UserSessionDto value, Duration ttl) {
		return reactiveRedisOps.opsForValue().set(key, value, ttl);
	}

	// READ
	public Mono<UserSessionDto> getValue(String key) {
		return reactiveRedisOps.opsForValue().get(key).switchIfEmpty(Mono.error(new RuntimeException("No Datas for key: " + key)));
	}

	// EXISTS
	public Mono<Boolean> hasKey(String key) {
		return reactiveRedisOps.hasKey(key);
	}

	public <T> Mono<T> getCacheValueGeneric(String key, Class<T> clazz) {
		try {
			return reactiveRedisOps.opsForValue().get(key).switchIfEmpty(Mono.error(new RuntimeException("No Datas for key: " + key)))
					.flatMap(value -> Mono.just(objectMapper.convertValue(value, clazz)));
		} catch (Exception e) {
			e.getStackTrace();
			return Mono.error(new RuntimeException("error occured!" + e.getMessage()));
		}
	}
}
