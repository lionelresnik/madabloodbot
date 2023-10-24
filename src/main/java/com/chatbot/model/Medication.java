package com.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String hebrew;

    @Parsed(field = "שם באנגלית")
    @JsonProperty("english")
    private String english;

    @Parsed(field = "שם גנרי")
    @JsonProperty("generic")
    private String generic;

    @Parsed(field = "הנחיות")
    @JsonProperty("instructions")
    private String instructions;

    @Parsed(field = "קריטריון")
    @JsonProperty("criterion")
    private String criterion;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("donation")
    private Boolean donation;

    // Add getters and setters for all fields

    public void setInstructions(String instructions) {
        this.instructions = instructions;
        this.donation = !instructions.contains("אסור להתרים");
    }

}
