package com.chatbot.service;


import com.chatbot.model.Medication;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author Lionel Resnik
 */
@Service
public class MedicationService {

    public Map<String, String> findMedications(List<String> medicationNames) {
        Map<String, String> instructions = new HashMap<>();
        try (FileReader reader = new FileReader("src/main/resources/medicines.csv")) {
            List<Medication> medications =
                    new CsvToBeanBuilder<Medication>(reader).withType(Medication.class).build().parse();

            for (Medication medication : medications) {
                if (medicationNames.contains(medication.getHebrewName()) ||
                    medicationNames.contains(medication.getEnglishName().toLowerCase()) ||
                    medicationNames.contains(medication.getEnglishName().toUpperCase()) ||
                    medicationNames.contains(medication.getGenericName())) {
                    instructions.put(medication.getHebrewName(), medication.getInstructions());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return instructions;
    }
}
