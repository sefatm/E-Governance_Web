package com.mgt.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString().replace("\\", "/") + "/";
        Path legacyUploadPath = Paths.get("src/main/resources/uploads").toAbsolutePath().normalize();
        String legacyUploadLocation = "file:" + legacyUploadPath.toString().replace("\\", "/") + "/";

        System.out.println("Upload serve path: " + uploadLocation);
        System.out.println("Legacy upload serve path: " + legacyUploadLocation);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation, legacyUploadLocation)
                .setCachePeriod(0);
    }
}
