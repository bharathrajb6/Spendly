package com.example.goal_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Sends a message to the Kafka topic with the given topic name and string data.
     *
     * @param topic the name of the topic to send the message to
     * @param data  the string data to send to the topic
     */
    public void sendTopic(String topic, String data) {
        kafkaTemplate.send(topic, data);
        log.info("Claim sent to Kafka: {}", data);
    }
}