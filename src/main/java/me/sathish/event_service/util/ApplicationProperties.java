package me.sathish.event_service.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sathishprojects")
public record ApplicationProperties(String sathishProjectEventsExchange, String githubRoutingKey) {}
