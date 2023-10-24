package com.chatbot.controller;

import com.chatbot.model.Medication;
import com.chatbot.model.MedicationFindResult;
import com.chatbot.service.AmazonS3Service;
import com.chatbot.service.MedicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lionel Resnik
 */

@RestController
@RequestMapping("/api/chat")
public class MedicationController {

    private static final Logger            log = LoggerFactory.getLogger(MedicationController.class);
    private final        MedicationService medicationService;
    private final        AmazonS3Service   amazonS3Service;
    @Value("${aws.bucket.name}")
    private              String            bucketName;
    @Value("${medications.file.path}")
    private              String            medicationsFilePath;

    public MedicationController(MedicationService medicationService, AmazonS3Service amazonS3Service) {
        this.medicationService = medicationService;
        this.amazonS3Service = amazonS3Service;
    }


    @PostMapping("/find")
    public MedicationFindResult processMedications(@RequestBody List<String> medications) {
        return medicationService.findMedications(medications);
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, List<Medication>> convertCsvToJson(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received request to convert CSV to JSON");

        log.info("Parsing file");
        List<Medication> medications = parseCsvFile(file);

        Map<String, List<Medication>> response = saveFile(medications, medicationsFilePath);
        log.info("Saving file locally");

        Path path = Paths.get(medicationsFilePath);
        log.info("creating path: " + path);

        log.info("trying to upload file to s3");
        amazonS3Service.uploadFile(bucketName, "medications.json", path);

        log.info("filling instructions from new file");
        medicationService.fillInstructions();


        log.info("Successfully done");

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

    private Map<String, List<Medication>> saveFile(List<Medication> medications, String today) throws IOException {
        Map<String, List<Medication>> response = new HashMap<>();
        response.put("medications", medications);

        // Convert object to JSON string
        ObjectMapper mapper     = new ObjectMapper();
        String       jsonString = mapper.writeValueAsString(response);

        // Define the path to the file
        Path path = Paths.get(medicationsFilePath);

        // Write the JSON string to the file
        Files.write(path, jsonString.getBytes());

        return response;
    }

}
