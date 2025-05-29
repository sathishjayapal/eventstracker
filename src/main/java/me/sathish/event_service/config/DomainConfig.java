package me.sathish.event_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("me.sathish.event_service")
@EnableJpaRepositories("me.sathish.event_service")
@EnableTransactionManagement
public class DomainConfig {
}
