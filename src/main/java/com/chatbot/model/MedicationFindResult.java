package com.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lionel Resnik
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicationFindResult {
    @JsonProperty("found")
    private List<Medication> found;

    @JsonProperty("partial_found")
    private List<Medication> partialFound;

    @JsonProperty("not_found")
    private List<MedicationNotFoundOutput> notFound;
}