package com.example.goal_service.service;

import com.example.goal_service.exception.CacheException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate redisTemplate;
    public ObjectMapper objectMapper = null;

    /**
     * Get an instance of the ObjectMapper.
     * If the ObjectMapper instance has not been set, a new instance is returned.
     * If the ObjectMapper instance has been set, the existing instance is returned.
     *
     * @return the ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            return new ObjectMapper();
        }
        return objectMapper;
    }

    /**
     * Get data from Redis Cache.
     *
     * @param key           the key of the data to retrieve from Redis
     * @param responseClass the class of the object to convert the data to
     * @param <T>           the type of the object to convert the data to
     * @return the data from Redis, converted to an object of type T
     * @throws CacheException if there is an error while getting data from Redis
     */
    public <T> T getData(String key, Class<T> responseClass) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                ObjectMapper mapper = getObjectMapper();
                if (responseClass.equals(List.class)) {
                    return (T) mapper.readValue(data.toString(), new TypeReference<List<T>>() {
                    });
                } else {
                    return mapper.readValue(data.toString(), responseClass);
                }
            }
        } catch (Exception exception) {
            throw new CacheException(exception.getMessage());
        }
        return null;
    }


    /**
     * Set data in Redis Cache.
     *
     * @param key    the key of the data to set in Redis
     * @param object the object to convert to JSON and store in Redis
     * @param ttl    the time to live of the data in seconds
     * @throws CacheException if there is an error while setting data in Redis
     */
    public void setData(String key, Object object, Long ttl) {
        try {
            ObjectMapper mapper = getObjectMapper();
            String jsonValue = mapper.writeValueAsString(object);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new CacheException(exception.getMessage());
        }
    }


    /**
     * Delete data from Redis Cache.
     *
     * @param key the key of the data to delete from Redis
     * @throws CacheException if there is an error while deleting data from Redis
     */
    public void deleteData(String key) {
        try {
            redisTemplate.opsForValue().getAndDelete(key);
        } catch (Exception exception) {
            throw new CacheException(exception.getMessage());
        }
    }
}


