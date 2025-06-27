package com.capstone.wellnessnavigatorgym.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for consistent time handling across the application
 * Fixed timezone to GMT+7 (Asia/Ho_Chi_Minh) to prevent timezone issues
 */
public class TimeUtil {
    
    // Fixed timezone for Vietnam
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    /**
     * Get current LocalDateTime in Vietnam timezone
     * This replaces all LocalDateTime.now() calls to ensure consistency
     */
    public static LocalDateTime now() {
        return ZonedDateTime.now(VIETNAM_ZONE).toLocalDateTime();
    }
    
    /**
     * Get LocalDateTime with specified hours added
     */
    public static LocalDateTime nowPlusHours(long hours) {
        return now().plusHours(hours);
    }
    
    /**
     * Get LocalDateTime with specified minutes added
     */
    public static LocalDateTime nowPlusMinutes(long minutes) {
        return now().plusMinutes(minutes);
    }
    
    /**
     * Get LocalDateTime with specified minutes subtracted
     */
    public static LocalDateTime nowMinusMinutes(long minutes) {
        return now().minusMinutes(minutes);
    }
    
    /**
     * Get current zone ID
     */
    public static ZoneId getTimeZone() {
        return VIETNAM_ZONE;
    }
} 