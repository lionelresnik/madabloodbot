package com.chatbot.controller;

import com.chatbot.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lionel Resnik
 */

@RestController
@RequestMapping("/chat")
public class ChatbotController {

    @Autowired
    private MedicationService medicationService;

    @Value("${bot.hello}")
    private String hello;

    @Value("${bot.question}")
    private String question;

    @Value("${bot.allowedDonation}")
    private String allowedDonation;

    @Value("${bot.medicationsQuestion}")
    private String medicationsQuestion;

    @Value("${bot.noMedicationsFound}")
    private String noMedicationsFound;

    // ... add more @Value annotations for other properties ...

    @GetMapping
    public String startChat() {
        return hello + "\n" + question;
    }

    @PostMapping
    public String processAnswer(@RequestBody String answer) {
        if ("לא".equals(answer)) {
            return allowedDonation;
        }
        else if ("כן".equals(answer)) {
            return medicationsQuestion;
        }
        else {
            return question;
        }
    }

    @PostMapping("/medications")
    public String processMedications(@RequestBody List<String> medications) {
        Map<String, String> instructions = medicationService.findMedications(medications);
        if (instructions.isEmpty()) {
            return noMedicationsFound;
        }
        else {
            return instructions.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                               .collect(Collectors.joining("\n"));
        }
    }
}
