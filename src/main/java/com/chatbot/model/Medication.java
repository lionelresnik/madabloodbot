package com.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.bean.CsvBindByName;
import com.univocity.parsers.annotations.Parsed;
import lombok.Data;

/**
 * @author Lionel Resnik
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Medication {

    @Parsed(field = "שם בעברית")
    @JsonProperty("hebrew")
    @CsvBindByName(column = "שם בעברית")
    private String hebrew;

    @Parsed(field = "שם באנגלית")
    @JsonProperty("english")
    @CsvBindByName(column = "שם באנגלית")
    private String english;

    @Parsed(field = "שם גנרי")
    @JsonProperty("generic")
    @CsvBindByName(column = "שם גנרי")
    private String generic;

    @Parsed(field = "הנחיות")
    @JsonProperty("instructions")
    @CsvBindByName(column = "הנחיות")
    private String instructions;

    @Parsed(field = "קריטריון")
    @JsonProperty("criterion")
    @CsvBindByName(column = "קריטריון")
    private String criterion;


    @JsonProperty("donation")
    private Boolean donation;

    // Add getters and setters for all fields

    public void setInstructions(String instructions) {
        this.instructions = instructions;
        this.donation = !instructions.contains("אסור להתרים");
    }

}
