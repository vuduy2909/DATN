package com.capstone.wellnessnavigatorgym.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BodyTypeConverter implements AttributeConverter<BodyType, String> {

    @Override
    public String convertToDatabaseColumn(BodyType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDatabaseValue();
    }

    @Override
    public BodyType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return BodyType.NORMAL; // Default value
        }
        
        return BodyType.fromDatabaseValue(dbData.trim());
    }
} 