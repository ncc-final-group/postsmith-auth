package com.postsmith.auth.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.postsmith.auth.dto.UserSessionDto;

import reactor.core.publisher.Mono;

@Service
public class RedisService {
	ReactiveRedisTemplate<String, UserSessionDto> redisTemplate;
	private static final String USER_KEY_PREFIX = "user:";

	// CREATE or UPDATE
	public Mono<Boolean> save(UserSessionDto user) {
		String key = USER_KEY_PREFIX + user.getSessionId();
		return redisTemplate.opsForValue().set(key, user);
	}

	// READ
	public Mono<UserSessionDto> findById(String sessionId) {
		String key = USER_KEY_PREFIX + sessionId;
		return redisTemplate.opsForValue().get(key);
	}

	// DELETE
	public Mono<Boolean> deleteById(String sessionId) {
		String key = USER_KEY_PREFIX + sessionId;
		return redisTemplate.opsForValue().delete(key);
	}

	// EXISTS
	public Mono<Boolean> exists(String sessionId) {
		String key = USER_KEY_PREFIX + sessionId;
		return redisTemplate.hasKey(key);
	}
}
