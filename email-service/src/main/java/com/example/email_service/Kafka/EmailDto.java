package com.example.email_service.Kafka;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto {
    public String userEmail;
    public String subject;
    public String body;
}
