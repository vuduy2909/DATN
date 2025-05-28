package com.capstone.wellnessnavigatorgym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WellnessNavigatorGymApplication {

    public static void main(String[] args) {
        SpringApplication.run(WellnessNavigatorGymApplication.class, args);
    }

}

