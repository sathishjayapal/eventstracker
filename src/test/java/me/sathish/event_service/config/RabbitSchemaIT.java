package me.sathish.event_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:postgresql://localhost:5445/eventstracker_db",
            "spring.datasource.username=postgres",
            "spring.datasource.password=P4ssword!",
            "spring.datasource.driver-class-name=org.postgresql.Driver",
            "spring.rabbitmq.host=localhost",
            "spring.rabbitmq.port=5672",
            "spring.rabbitmq.username=guest",
            "spring.rabbitmq.password=guest",
            "eventDomainUser=user",
            "eventDomainUserPassword=pass"
        })
class RabbitSchemaIT {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    void allQueuesAreDeclared() {
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.SAT_PROJECTS_EVENTS_QUEUE))
                .as("Catch-all events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.DLQ_SAT_PROJECTS_EVENTS_QUEUE))
                .as("DLQ for catch-all events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.GITHUB_API_EVENTS_QUEUE))
                .as("GitHub API events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.GITHUB_OPS_EVENTS_QUEUE))
                .as("GitHub ops events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.DLQ_GITHUB_API_EVENTS_QUEUE))
                .as("DLQ for GitHub API events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.DLQ_GITHUB_OPS_EVENTS_QUEUE))
                .as("DLQ for GitHub ops events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.GARMIN_API_EVENTS_QUEUE))
                .as("Garmin API events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.GARMIN_OPS_EVENTS_QUEUE))
                .as("Garmin ops events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.DLQ_GARMIN_API_EVENTS_QUEUE))
                .as("DLQ for Garmin API events queue should exist")
                .isNotNull();
        assertThat(amqpAdmin.getQueueProperties(RabbitSchemaConfig.DLQ_GARMIN_OPS_EVENTS_QUEUE))
                .as("DLQ for Garmin ops events queue should exist")
                .isNotNull();
    }
}
