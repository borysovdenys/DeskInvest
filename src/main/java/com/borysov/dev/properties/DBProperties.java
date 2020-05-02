package com.borysov.dev.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties( prefix = "spring.datasource")
@Component
public class DBProperties {
    private String username;
    private String password;
    private String url;
    private String driverClassName;
}
