package com.example.transaction_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Sends a message to the Kafka topic with the given topic name and string data.
     * This method is async and non-blocking - it will not wait for Kafka.
     *
     * @param topic the name of the topic to send the message to
     * @param data  the string data to send to the topic
     */
    @Async
    public void sendTopic(String topic, String data) {
        try {
            kafkaTemplate.send(topic, data)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Failed to send message to Kafka topic {}: {}", topic, ex.getMessage());
                        } else {
                            log.info("Message sent to Kafka topic {}: {}", topic, data);
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to send to Kafka: {}", e.getMessage());
        }
    }
}
