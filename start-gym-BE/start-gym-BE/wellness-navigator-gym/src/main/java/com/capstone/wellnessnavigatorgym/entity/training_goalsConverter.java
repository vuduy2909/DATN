package com.capstone.wellnessnavigatorgym.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class training_goalsConverter implements AttributeConverter<training_goals, String> {

    @Override
    public String convertToDatabaseColumn(training_goals attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDatabaseValue();
    }

    @Override
    public training_goals convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return training_goals.WEIGHT_LOSS; // Default value
        }
        
        return training_goals.fromDatabaseValue(dbData.trim());
    }
} 