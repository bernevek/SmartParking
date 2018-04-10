package com.smartparking.eventprocessor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("eventprocessor.httpclient")
public class HttpClientProperties {
    private String serverUrl;
    private String email;
    private String password;
}
