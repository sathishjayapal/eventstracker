package com.sathish.runevents;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(CorsProperties cors) {
    public record CorsProperties(
            @DefaultValue("/**") String pathPattern,
            @DefaultValue("*") String allowedOrigins,
            @DefaultValue("*") String allowedMethods,
            @DefaultValue("*") String allowedHeaders) {}
}
