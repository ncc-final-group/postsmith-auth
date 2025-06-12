package com.postsmith.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class RedisService {
	private ReactiveRedisTemplate<String, Object> redisTemplate;

	// CREATE
	public Mono<Boolean> setValue(String key, Object value, Duration ttl) {
		return redisTemplate.opsForValue().set(key, value, ttl);
	}

	// READ
	public Mono<Object> getValue(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	// DELETE
	public Mono<Boolean> deleteKey(String key) {
		return redisTemplate.opsForValue().delete(key);
	}

	// EXISTS
	public Mono<Boolean> hasKey(String key) {
		return redisTemplate.hasKey(key);
	}
}
