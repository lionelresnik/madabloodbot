package com.chatbot;

import com.chatbot.service.MedicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author Lionel Resnik
 */
@SpringBootApplication
public class ChatbotApplication {

    private static final Logger log = LoggerFactory.getLogger(ChatbotApplication.class);

    private final MedicationService medicationService;

    public ChatbotApplication(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
    }

    @PostConstruct
    public void init() {

        try {
            medicationService.fillInstructions();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}