package com.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Lionel Resnik
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicationNotFoundOutput {

    @JsonProperty("medication_name")
    private String name;
}
