package com.app.e_commerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {
    private String defaultAvatarPath = "static/images/avatar.png";
    private int maxWidth = 800;
    private int maxHeight = 800;
    private float compressionQuality = 0.7f;
    private long maxFileSize = 1024 * 1024; // 1MB
}
