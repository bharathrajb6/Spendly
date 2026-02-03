package com.example.user_service.service;

import com.example.user_service.exceptions.CacheException;
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

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieves the ObjectMapper instance for JSON serialization/deserialization.
     *
     * @return the ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Retrieves data from Redis cache based on the provided key.
     * Deserializes the JSON data into the target response class.
     *
     * @param key           the unique identifier for the cached data
     * @param responseClass the class type to which the data should be deserialized
     * @param <T>           the type of the response object
     * @return the deserialized object, or null if no data is found for the key
     * @throws CacheException if an error occurs during retrieval or deserialization
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
     * Stores data in the Redis cache with a specified time-to-live (TTL).
     * Serializes the object into a JSON string before storing.
     *
     * @param key    the unique identifier for the data to be cached
     * @param object the object to store in the cache
     * @param ttl    the time-to-live in seconds for the cached data
     * @throws CacheException if an error occurs during serialization or storage
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
     * Deletes data associated with the specified key from the Redis cache.
     *
     * @param key the unique identifier for the data to be removed
     * @throws CacheException if an error occurs during deletion
     */
    public void deleteData(String key) {
        try {
            redisTemplate.opsForValue().getAndDelete(key);
        } catch (Exception exception) {
            throw new CacheException(exception.getMessage());
        }
    }
}
