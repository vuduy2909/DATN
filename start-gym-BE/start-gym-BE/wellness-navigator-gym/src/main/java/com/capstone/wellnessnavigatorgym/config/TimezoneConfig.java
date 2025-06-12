package com.capstone.wellnessnavigatorgym.config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {
    
    @PostConstruct
    public void setTimeZone() {
        // Set application timezone to Vietnam timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println("Application timezone set to: " + TimeZone.getDefault().getID());
        System.out.println("All times will be in Vietnam timezone - frontend no need to convert");
    }
} 