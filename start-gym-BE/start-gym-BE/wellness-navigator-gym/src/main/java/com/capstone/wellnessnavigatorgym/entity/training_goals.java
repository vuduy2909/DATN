package com.capstone.wellnessnavigatorgym.entity;

public enum training_goals {
    STRENGTH_TRAINING("1, Strength Training"),
    CARDIOVASCULAR_TRAINING("2, Cardiovascular Training"),
    FLEXIBILITY_AND_BALANCE("3, Flexibility and Balance"),
    TRAIN_STRENGTH_AND_ENDURANCE("4, Train strength and endurance"),
    MIND_BODY_WELLNESS("5, Mind-Body Wellness"),
    FUNCTIONAL_TRAINING("6, Functional Training"),
    WEIGHT_LOSS("7, Weight Loss");
    
    private final String databaseValue;
    
    training_goals(String databaseValue) {
        this.databaseValue = databaseValue;
    }
    
    public String getDatabaseValue() {
        return databaseValue;
    }
    
    public static training_goals fromDatabaseValue(String value) {
        if (value == null) return WEIGHT_LOSS;
        
        for (training_goals goal : values()) {
            if (goal.databaseValue.equals(value.trim())) {
                return goal;
            }
        }
        return WEIGHT_LOSS;
    }
    
    @Override
    public String toString() {
        return databaseValue;
    }
} 