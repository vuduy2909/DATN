package com.capstone.wellnessnavigatorgym.entity;

public enum WorkEnvironment {
    Office("Office"),
    SHIFT_WORK("Shift work"),
    STANDING_INTENSIVE("Standing-intensive"),
    WALKING_INTENSIVE("Walking-intensive"),
    DRIVING_INTENSIVE("Driving-intensive"),
    MANUAL_LABOR("Manual labor");
    
    private final String value;
    
    WorkEnvironment(String value) {
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