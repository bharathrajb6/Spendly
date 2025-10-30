package com.example.goal_service.config;


import com.example.goal_service.model.TransactionDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    /**
     * Producer factory for publishing JSON payloads.
     *
     * @return configured ProducerFactory for String keys and JSON values
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        log.info("Initialized Kafka ProducerFactory for goal-service");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate for sending messages.
     *
     * @return KafkaTemplate instance
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        log.info("Created KafkaTemplate for goal-service");
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Return a {@link Map} of common properties shared by all Kafka listeners.
     *
     * @return a {@link Map} with common Kafka listener properties
     */
    /**
     * Base consumer configuration shared across listener container factories.
     *
     * @return map of base Kafka consumer properties
     */
    private Map<String, Object> baseConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "goal-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.goal_service.dto,com.example.transaction_service.model,com.example.goal_service.model");
        log.info("Initialized base Kafka consumer properties for goal-service");
        return props;
    }


    /**
     * Returns a {@link ConsumerFactory} suitable for the given {@code targetType}.
     *
     * @param targetType the target type for deserialization
     * @return a {@link ConsumerFactory} that can create a consumer for the given type
     */
    /**
     * Builds a ConsumerFactory for a specific target type.
     *
     * @param targetType payload class to deserialize to
     * @return ConsumerFactory instance
     */
    public <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        return new DefaultKafkaConsumerFactory<>(
                baseConfig(),
                new StringDeserializer(),
                new JsonDeserializer<>(targetType, false) // false = disable type headers
        );
    }

    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} that can be used to create listeners that consume
     * Kafka messages of the given type.
     *
     * @param targetType the type of the payloads in the Kafka messages
     * @param <T>        the type of the payloads in the Kafka messages
     * @return a {@link ConcurrentKafkaListenerContainerFactory} that can be used to create listeners that consume
     * Kafka messages of the given type
     */
    /**
     * Listener container factory for the given payload type.
     *
     * @param targetType payload type class
     * @param <T>        generic payload type
     * @return configured listener container factory
     */
    @Bean
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> kafkaListenerContainerFactory(Class<T> targetType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(targetType));
        return factory;
    }


    /**
     * Returns a {@link ConcurrentKafkaListenerContainerFactory} suitable for consuming "claims-verified" Kafka messages.
     *
     * @return a {@link ConcurrentKafkaListenerContainerFactory} that can be used to create listeners that consume
     * "claims-verified" Kafka messages
     */
    /**
     * Listener container factory for ClaimData payloads.
     *
     * @return configured listener container factory for ClaimData
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionDto> transactionDataKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionDto> factory = kafkaListenerContainerFactory(TransactionDto.class);
        log.info("Configured Transaction Kafka listener container factory for goal-service");
        return factory;
    }
}

