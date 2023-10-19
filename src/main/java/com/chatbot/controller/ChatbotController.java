package com.chatbot.controller;

import com.chatbot.model.Medication;
import com.chatbot.model.MedicationFindResult;
import com.chatbot.service.MedicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * @author Lionel Resnik
 */

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    @Autowired
    private MedicationService medicationService;

    @PostMapping("/find")
    public MedicationFindResult processMedications(@RequestBody List<String> medications) {
        MedicationFindResult instructions = medicationService.findMedications(medications);
        return instructions;
    }


    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, List<Medication>> convertCsvToJson(@RequestParam("file") MultipartFile file) throws IOException {
        List<Medication>              medications = parseCsvFile(file);
        Map<String, List<Medication>> response    = saveFile(medications);
        medicationService.fillInstructions();
        return response;
    }

    private List<Medication> parseCsvFile(MultipartFile file) throws IOException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvRoutines routines = new CsvRoutines(settings);

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return routines.parseAll(Medication.class, reader);
        }
    }

    private Map<String, List<Medication>> saveFile(List<Medication> medications) throws IOException {
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
