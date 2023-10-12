package com.chatbot.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

/**
 * @author Lionel Resnik
 */
@Data
public class Medication {

    @CsvBindByName(column = "שם בעברית")
    private String hebrewName;

    @CsvBindByName(column = "שם באנגלית")
    private String englishName;

    @CsvBindByName(column = "שם גנרי")
    private String genericName;

    @CsvBindByName(column = "הנחיות")
    private String instructions;

}
