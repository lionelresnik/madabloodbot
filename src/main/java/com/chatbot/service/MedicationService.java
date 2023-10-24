package com.chatbot.service;


import com.chatbot.model.Medication;
import com.chatbot.model.MedicationFindResult;
import com.chatbot.model.MedicationNotFoundOutput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Lionel Resnik
 */
@Service
public class MedicationService {

    private static final Logger          log          = LoggerFactory.getLogger(MedicationService.class);
    private static final int             INDEX_LENGTH = 3;
    private final        AmazonS3Service amazonS3Service;
    @Value("${aws.bucket.name}")
    private              String          bucketName;
    @Value("${medications.file.path}")
    private              String          medicationsFilePath;

    private final Map<String, Medication>   hebrewInstructions  = new HashMap<>();
    private final Map<String, Medication>   englishInstructions = new HashMap<>();
    private final Map<String, Medication>   genericInstructions = new HashMap<>();
    private final Map<String, List<String>> index               = new HashMap<>();

    public MedicationService(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    public void fillInstructions() throws IOException {
        // Try to download the file from Amazon S3
        try {
            amazonS3Service.downloadFile(bucketName, "medications.json");
        }
        catch (Exception e) {
            log.warn("Failed to download file from Amazon S3, using local file instead", e);
            // If the download fails, use the local file
        }
        Path path = Paths.get(medicationsFilePath);
        if (Files.exists(path)) {
            log.info("Filling instructions from file");
            fillInstructionsFromFile(path);
        }

    }

    private void fillInstructionsFromFile(Path path) throws IOException {
        // Create an ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // Define the TypeReference for List<Medication>
        TypeReference<HashMap<String, List<Medication>>> typeRef = new TypeReference<>() {
        };

        // Read the file and deserialize it into a list of Medication objects
        HashMap<String, List<Medication>> response = mapper.readValue(path.toFile(), typeRef);

        // Get the list of Medication objects
        List<Medication> medications = response.get("medications");

        // Fill the instruction maps
        for (Medication medication : medications) {
            hebrewInstructions.put(medication.getHebrew(), medication);
            englishInstructions.put(medication.getEnglish().toLowerCase(), medication);
            genericInstructions.put(medication.getGeneric().toLowerCase(), medication);
        }

        // Fill the index
        index.clear();
        addToIndex(hebrewInstructions.keySet());
        addToIndex(englishInstructions.keySet());
        addToIndex(genericInstructions.keySet());
    }

    private void addToIndex(Set<String> names) {
        for (String name : names) {
            String key = name.substring(0, Math.min(name.length(), INDEX_LENGTH)).toLowerCase();
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(name);
        }
    }


    public MedicationFindResult findMedications(List<String> medicationNames) {
        List<Medication>               found        = new ArrayList<>();
        List<MedicationNotFoundOutput> notFound     = new ArrayList<>();
        List<Medication>               partialFound = new ArrayList<>();

        for (String name : medicationNames) {
            Medication medication = getInstructions(name);

            if (medication != null) {
                found.add(medication);
            }
            else {
                String similarName = findSimilarName(name);
                if (similarName != null) {
                    medication = getInstructions(similarName);
                    partialFound.add(medication);
                }
                else {
                    MedicationNotFoundOutput medicationMissing = new MedicationNotFoundOutput();
                    medicationMissing.setName(name);
                    notFound.add(medicationMissing);
                }
            }
        }

        MedicationFindResult result = new MedicationFindResult();
        result.setFound(found);
        result.setPartialFound(partialFound);
        result.setNotFound(notFound);
        return result;
    }


    private String findSimilarName(String name) {
        LevenshteinDistance distanceCalculator = new LevenshteinDistance();
        int                 threshold          = 2;  // adjust this value as needed

        String       key        = name.substring(0, Math.min(name.length(), INDEX_LENGTH)).toLowerCase();
        List<String> candidates = index.get(key.toLowerCase());

        if (candidates != null) {
            for (String candidate : candidates) {
                if (distanceCalculator.apply(name.toLowerCase(), candidate) <= threshold) {
                    return candidate;
                }
            }
        }

        // No similar name found
        return null;
    }

    private Medication getInstructions(String name) {

        Medication medication = null;
        if (hebrewInstructions.containsKey(name)) {
            medication = hebrewInstructions.get(name);
            return medication;
        }
        else if (englishInstructions.containsKey(name.toLowerCase())) {
            medication = englishInstructions.get(name.toLowerCase());
            return medication;

        }
        else if (genericInstructions.containsKey(name.toLowerCase())) {
            medication = genericInstructions.get(name.toLowerCase());
        }

        return medication;
    }

    public Map<String, Medication> getHebrewInstructions() {
        return hebrewInstructions;
    }

    public Map<String, Medication> getEnglishInstructions() {
        return englishInstructions;
    }

    public Map<String, Medication> getGenericInstructions() {
        return genericInstructions;
    }
}
