package com.example.goal_service.config;

import com.example.goal_service.model.TransactionDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Initializes a Kafka producer factory for goal-service.
     * <p>
     * This factory is configured with the bootstrap server, key serializer as
     * StringSerializer, and value serializer as JsonSerializer.
     *
     * @return a DefaultKafkaProducerFactory instance
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        log.info("Initializing Kafka ProducerFactory for goal-service with bootstrap servers: {}", bootstrapServers);
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        log.info("Initialized Kafka ProducerFactory for goal-service with properties: {}", configProps);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Returns a KafkaTemplate that can be used to send and receive messages.
     * <p>
     * The returned template is configured with the producer factory, which is
     * responsible for serializing and sending messages.
     *
     * @return a configured KafkaTemplate instance
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        log.info("Initializing KafkaTemplate for goal-service");
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        log.info("Initialized KafkaTemplate for goal-service with producer factory: {}", producerFactory());
        return kafkaTemplate;
    }

    /**
     * Initializes base Kafka consumer properties for goal-service.
     *
     * <p>
     * These properties include the bootstrap server, group ID, key deserializer,
     * value deserializer, and trusted packages.
     *
     * @return a map of Kafka consumer properties
     */
    private Map<String, Object> baseConfig() {
        log.info("Initializing base Kafka consumer properties for goal-service with bootstrap servers: {}",
                bootstrapServers);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        log.info("Added bootstrap server property to base Kafka consumer properties: {}",
                props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "goal-service");
        log.info("Added group ID property to base Kafka consumer properties: {}",
                props.get(ConsumerConfig.GROUP_ID_CONFIG));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        log.info("Added key deserializer property to base Kafka consumer properties: {}",
                props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        log.info("Added value deserializer property to base Kafka consumer properties: {}",
                props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        props.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.example.goal_service.dto,com.example.transaction_service.model,com.example.goal_service.model");
        log.info("Added trusted packages property to base Kafka consumer properties: {}",
                props.get(JsonDeserializer.TRUSTED_PACKAGES));
        log.info("Initialized base Kafka consumer properties for goal-service with properties: {}", props);
        return props;
    }

    /**
     * Returns a {@link ConsumerFactory} that can be used to create Kafka consumers
     * that consume messages of the given type.
     * The returned factory is configured with the base Kafka consumer properties
     * and uses the given type for deserialization.
     *
     * @param targetType the type of the payloads in the Kafka messages
     * @param <T>        the type of the payloads in the Kafka messages
     * @return a {@link ConsumerFactory} that can be used to create Kafka consumers
     *         that consume messages of the given type
     */
    public <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        return new DefaultKafkaConsumerFactory<>(
                baseConfig(),
                new StringDeserializer(),
                new JsonDeserializer<>(targetType, false) // false = disable type headers
        );
    }

    /**
     * Returns a {@link ConcurrentKafkaListenerContainerFactory} that can be used to
     * create listeners that consume
     * Kafka messages of the given type.
     *
     * @param targetType the type of the payloads in the Kafka messages
     * @param <T>        the type of the payloads in the Kafka messages
     * @return a {@link ConcurrentKafkaListenerContainerFactory} that can be used to
     *         create listeners that consume
     *         Kafka messages of the given type
     */
    @Bean
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> kafkaListenerContainerFactory(Class<T> targetType) {
        log.info("Initializing Kafka listener container factory for type: {}", targetType.getName());
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        log.info("Setting consumer factory for type: {}", targetType.getName());
        factory.setConsumerFactory(consumerFactory(targetType));
        log.info("Initialized Kafka listener container factory for type: {}", targetType.getName());
        return factory;
    }

    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} that can be used to
     * create listeners that consume
     * Kafka messages of type {@link TransactionDto}.
     *
     * @return a configured listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionDto> transactionDataKafkaListenerContainerFactory() {
        log.info("Initializing Kafka listener container factory for TransactionDto");
        ConcurrentKafkaListenerContainerFactory<String, TransactionDto> factory = kafkaListenerContainerFactory(
                TransactionDto.class);
        log.info("Configured Transaction Kafka listener container factory for goal-service");
        return factory;
    }
}
