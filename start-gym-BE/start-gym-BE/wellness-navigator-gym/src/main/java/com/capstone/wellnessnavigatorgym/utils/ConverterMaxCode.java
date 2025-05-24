package com.capstone.wellnessnavigatorgym.utils;

public class ConverterMaxCode {
    public static String generateNextId(String currentId) {
        if (currentId == null || currentId.trim().isEmpty()) {
            // If no customer exists, start with CUST-000001
            return "CUST-000001";
        }
        
        // Handle new format like "CUST-000001", "CUST-000002"
        if (currentId.contains("-")) {
            String[] parts = currentId.split("-");
            if (parts.length >= 2) {
                String prefix = parts[0]; // "CUST"
                try {
                    int number = Integer.parseInt(parts[1]); // "000001" -> 1
                    int nextNumber = number + 1;
                    
                    // Format with dash: CUST-000001, CUST-000002, etc.
                    return String.format("%s-%06d", prefix, nextNumber);
                } catch (NumberFormatException e) {
                    // If parsing fails, generate default
                    return generateDefaultNext();
                }
            }
        }
        
        // Handle old format without dash like "CUST000001", "CUST000002" 
        if (currentId.startsWith("CUST") && currentId.length() >= 8) {
            try {
                // Extract the number part (last 6 digits)
                String numberStr = currentId.substring(4); // Skip "CUST"
                int number = Integer.parseInt(numberStr);
                int nextNumber = number + 1;
                
                // Convert to new format with dash
                return String.format("CUST-%06d", nextNumber);
            } catch (NumberFormatException e) {
                // If parsing fails, generate default
                return generateDefaultNext();
            }
        }
        
        // Handle other legacy formats for backward compatibility
        String prefix = extractPrefix(currentId);
        String numberStr = extractNumber(currentId);
        
        try {
            int number = Integer.parseInt(numberStr);
            int nextNumber = number + 1;
            
            // Always use CUST format with dash as standard
            return String.format("CUST-%06d", nextNumber);
        } catch (NumberFormatException e) {
            // If all else fails, generate a default
            return generateDefaultNext();
        }
    }
    
    private static String extractPrefix(String code) {
        // Extract alphabetic prefix from codes like "TEST001", "CUST000001"
        StringBuilder prefix = new StringBuilder();
        for (char c : code.toCharArray()) {
            if (Character.isLetter(c)) {
                prefix.append(c);
            } else {
                break;
            }
        }
        return prefix.length() > 0 ? prefix.toString() : "CUST";
    }
    
    private static String extractNumber(String code) {
        // Extract numeric suffix from codes like "TEST001", "CUST000001"
        StringBuilder number = new StringBuilder();
        boolean foundDigit = false;
        for (char c : code.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
                foundDigit = true;
            } else if (foundDigit) {
                // Stop if we hit a non-digit after finding digits
                break;
            }
        }
        return number.length() > 0 ? number.toString() : "0";
    }
    
    private static String generateDefaultNext() {
        // Generate a simple incremental code with dash
        long timestamp = System.currentTimeMillis() % 1000000;
        return String.format("CUST-%06d", timestamp);
    }
}
