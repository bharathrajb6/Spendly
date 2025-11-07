package com.example.goal_service.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

@Configuration
@Slf4j
public class RedisConfig {

    /**
     * Create a RedisTemplate bean for the application.
     * <p>
     * This method creates a RedisTemplate instance and configures it with a RedisConnectionFactory and StringRedisSerializer.
     *
     * @param connectionFactory the Redis connection factory to use
     * @return the configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Initializing RedisTemplate for goal-service");
        RedisTemplate redisTemplate = new RedisTemplate();
        log.info("Setting connection factory for RedisTemplate");
        redisTemplate.setConnectionFactory(connectionFactory);
        log.info("Setting value serializer for RedisTemplate to StringRedisSerializer");
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        log.info("Setting key serializer for RedisTemplate to StringRedisSerializer");
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        log.info("Initialized RedisTemplate for goal-service");
        return redisTemplate;
    }
}

