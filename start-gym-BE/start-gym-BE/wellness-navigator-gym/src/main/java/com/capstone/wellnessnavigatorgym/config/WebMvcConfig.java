package com.capstone.wellnessnavigatorgym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from the "uploads" directory
        exposeDirectory("uploads", registry);
    }
    
    private void exposeDirectory(String directoryName, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(directoryName);
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        if (uploadPath.endsWith("/")) {
            registry.addResourceHandler("/" + directoryName + "/**")
                .addResourceLocations("file:" + uploadPath);
        } else {
            registry.addResourceHandler("/" + directoryName + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
        }
    }
} 