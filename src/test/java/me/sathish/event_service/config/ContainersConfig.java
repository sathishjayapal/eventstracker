package me.sathish.event_service.config;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

/**
 * One Postgres and one RabbitMQ for the whole test JVM, shared by every Spring context.
 *
 * <p>Declaring a fresh container in each {@code @Bean} method meant every distinct cached context
 * (BaseIT subclasses, @TestPropertySource variants, RabbitSchemaIT, ...) started its own pair and kept
 * it alive until the JVM exited. On a memory-constrained Docker the third RabbitMQ intermittently
 * missed its 2-minute start-up window. The containers are static singletons; {@code destroyMethod = ""}
 * stops Spring closing them with the first context that shuts down. Testcontainers' Ryuk reaps them
 * when the JVM exits.
 */
@SuppressWarnings("resource") // singletons by design; closed by Ryuk at JVM exit
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine").withDatabaseName("eventstracker_db");

    private static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer("rabbitmq:3.12-management-alpine").withStartupTimeout(Duration.ofMinutes(2));

    @Bean(destroyMethod = "")
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES;
    }

    @Bean(destroyMethod = "")
    @ServiceConnection
    RabbitMQContainer rabbitMQContainer() {
        return RABBITMQ;
    }
}
