package com.capstone.wellnessnavigatorgym.entity;

public enum Occupation {
    Student("Student"), 
    OFFICE_WORKER("Office worker"), 
    DELIVERY_STAFF("Delivery staff"), 
    Lecturer("Lecturer"), 
    SERVICE_STAFF("Service staff"), 
    Other("Other");
    
    private final String value;
    
    Occupation(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
} 