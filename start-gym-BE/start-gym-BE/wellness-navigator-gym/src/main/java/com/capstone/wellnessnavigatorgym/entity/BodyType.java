package com.capstone.wellnessnavigatorgym.entity;

public enum BodyType {
    THIN("Thin"),
    SKINNY_FAT("Skinny Fat"), 
    OVERWEIGHT("Overweight"),
    NORMAL("Normal"),
    FIT("Fit");
    
    private final String databaseValue;
    
    BodyType(String databaseValue) {
        this.databaseValue = databaseValue;
    }
    
    public String getDatabaseValue() {
        return databaseValue;
    }
    
    public static BodyType fromDatabaseValue(String value) {
        if (value == null) return NORMAL;
        
        for (BodyType type : values()) {
            if (type.databaseValue.equals(value.trim())) {
                return type;
            }
        }
        return NORMAL; // Default
    }
    
    @Override
    public String toString() {
        return databaseValue;
    }
} 