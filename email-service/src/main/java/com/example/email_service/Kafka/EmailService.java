package com.example.email_service.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * Listens to messages on the "send-email" topic and sends an email to the user specified in the message.
     * The email is sent using the JavaMailSender.
     * The subject and body of the email are set using the information in the EmailDto object.
     * The email is sent to the email address of the user returned by the getUserEmail method.
     *
     * @param emailDto the EmailDto object containing the information about the email to be sent
     */
    @KafkaListener(topics = "send-email", groupId = "email-service", containerFactory = "emailDataKafkaListenerContainerFactory")
    public void sendEmail(EmailDto emailDto) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(getUserEmail(emailDto.getUserEmail()));
        simpleMailMessage.setSubject(emailDto.getSubject());
        simpleMailMessage.setText(emailDto.getBody());

        javaMailSender.send(simpleMailMessage);

        log.info("Email sent to {}", emailDto.getUserEmail());
    }

    /**
     * Makes a GET request to the user-service to get the email address of the user specified by the username.
     * If the user-service is unavailable, the method throws a RuntimeException with a message indicating that the user-service is unavailable.
     * If the user is not found, the method throws a RuntimeException with a message indicating that the user is not found.
     *
     * @param username the username of the user whose email address is to be retrieved
     * @return the email address of the user
     * @throws RuntimeException if the user-service is unavailable or the user is not found
     */
    private String getUserEmail(String username) {

        String url = "http://localhost:8081/api/v1/users/" + username;
        ResponseEntity<String> response;

        try {
            response = restTemplate.getForEntity(url, String.class);
        } catch (RestClientResponseException exception) {
            throw new RuntimeException(exception.getMessage());
        } catch (ResourceAccessException exception) {
            log.error("Upstream user-service unavailable", exception);
            throw new RuntimeException(exception.getMessage());
        }

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return response.getBody();
    }
}
