package com.chatbot.controller;

import com.chatbot.model.Medication;
import com.chatbot.service.MedicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Lionel Resnik
 */

@RestController
@RequestMapping("/api/chat")
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


    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, List<Medication>> convertCsvToJson(@RequestParam("file") MultipartFile file) throws IOException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvRoutines routines = new CsvRoutines(settings);

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            List<Medication> medications = routines.parseAll(Medication.class, reader);

            Map<String, List<Medication>> response = new HashMap<>();
            response.put("medications", medications);

            // Convert object to JSON string
            ObjectMapper mapper     = new ObjectMapper();
            String       jsonString = mapper.writeValueAsString(response);

            // Define the path to the file
            Path path = Paths.get("src/main/resources/medications.json");

            // Check if file already exists
            if (Files.exists(path)) {
                // Get today's date and use it to create a new directory
                String today  = LocalDate.now().toString();
                Path   newDir = Paths.get("src/main/resources/" + today);

                // Create the directory if it doesn't exist
                if (!Files.exists(newDir)) {
                    Files.createDirectories(newDir);
                }

                // Get the files in the directory
                AtomicInteger counter = new AtomicInteger(1);
                Files.list(newDir).forEach(existingFile -> counter.getAndIncrement());

                // Rename the old file
                Files.move(path, newDir.resolve("medications." + counter.get() + ".json"));
            }

            // Write the JSON string to the file
            Files.write(path, jsonString.getBytes());

            return response;
        }
    }

}
