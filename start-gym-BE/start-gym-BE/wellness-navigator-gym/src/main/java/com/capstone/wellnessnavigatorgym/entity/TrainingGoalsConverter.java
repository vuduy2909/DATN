package com.capstone.wellnessnavigatorgym.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TrainingGoalsConverter implements AttributeConverter<TrainingGoals, String> {

    @Override
    public String convertToDatabaseColumn(TrainingGoals attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDatabaseValue();
    }

    @Override
    public TrainingGoals convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return TrainingGoals.WEIGHT_LOSS; // Default value
        }
        
        return TrainingGoals.fromDatabaseValue(dbData.trim());
    }
} 