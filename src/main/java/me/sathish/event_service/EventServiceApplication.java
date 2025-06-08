package me.sathish.event_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class EventServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
