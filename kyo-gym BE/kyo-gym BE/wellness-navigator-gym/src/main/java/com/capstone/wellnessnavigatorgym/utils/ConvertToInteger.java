package com.capstone.wellnessnavigatorgym.utils;

public class ConvertToInteger {
    public static Integer convertToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                // Handle the case where the conversion fails
                return null; // or throw an exception, depending on your requirements
            }
        } else {
            return null; // or handle other data types as needed
        }
    }
}
