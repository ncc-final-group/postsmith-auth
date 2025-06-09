package com.postsmith.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.postsmith.auth.dto.UserSessionDto;

@Configuration
@EnableAutoConfiguration(exclude={RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
public class RedisConfig {
	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Bean
	ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
		config.setPassword(RedisPassword.of(password));
		return new LettuceConnectionFactory(config);
	}

	@Bean
	ReactiveRedisOperations<String, UserSessionDto> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
		Jackson2JsonRedisSerializer<UserSessionDto> serializer = new Jackson2JsonRedisSerializer<>(UserSessionDto.class);
		RedisSerializationContext.RedisSerializationContextBuilder<String, UserSessionDto> builder = RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
		RedisSerializationContext<String, UserSessionDto> context = builder.value(serializer).hashValue(serializer).hashKey(serializer).build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
	}
}
