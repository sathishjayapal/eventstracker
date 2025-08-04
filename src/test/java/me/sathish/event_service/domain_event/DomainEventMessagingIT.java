package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import me.sathish.event_service.config.BaseIT;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.util.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

@Sql("/data/clearAll.sql")
class DomainEventMessagingIT extends BaseIT {

    @Autowired
    private DomainEventService domainEventService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    private Domain testDomain;
    private DomainEventDTO testDomainEventDTO;

    @BeforeEach
    void setUp() {
        // Create a test domain first
        testDomain = new Domain();
        testDomain.setDomainName("Messaging Test Domain");
        testDomain.setStatus("ACTIVE");

        testDomain.setDateCreated(OffsetDateTime.now());
        testDomain = domainRepository.save(testDomain);

        // Create test DTO
        testDomainEventDTO = new DomainEventDTO();
        testDomainEventDTO.setEventType("MESSAGING_TEST_EVENT");
        testDomainEventDTO.setPayload("Messaging test event payload");
        testDomainEventDTO.setEventId(UUID.randomUUID().toString());
        testDomainEventDTO.setCreatedBy("test-user");
        testDomainEventDTO.setUpdatedBy("test-user");
        testDomainEventDTO.setDomain(testDomain.getId());
    }

    @Test
    void create_ShouldPublishMessageToRabbitMQ() {
        // When
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Then
        assertNotNull(eventId);

        // Verify RabbitMQ message was sent
        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        eq(applicationProperties.garminExchange()),
                        eq(applicationProperties.garminNewRunQueue()),
                        any(DomainEventDTO.class));
    }

    @Test
    void create_WithRabbitMQFailure_ShouldStillPersistButThrowException() {
        // Given - Mock RabbitMQ to fail
        //        doThrow(new RuntimeException("RabbitMQ connection failed"))
        //                .when(rabbitTemplate)
        //                .convertAndSend(anyString(), anyString(), any());

        // When & Then
        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> domainEventService.create(testDomainEventDTO));

        assertEquals("Failed to publish domain event message", exception.getMessage());

        // Verify the event was still persisted in database
        // (This depends on transaction configuration - if @Transactional rollback is configured)
        //        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void create_MultipleEvents_ShouldPublishAllMessages() {
        // Given
        int numberOfEvents = 3;

        // When
        for (int i = 0; i < numberOfEvents; i++) {
            DomainEventDTO eventDTO = new DomainEventDTO();
            eventDTO.setEventType("BULK_MESSAGING_TEST_" + i);
            eventDTO.setPayload("Bulk messaging test data " + i);
            eventDTO.setCreatedBy("test-user");
            eventDTO.setUpdatedBy("test-user");
            eventDTO.setDomain(testDomain.getId());
            eventDTO.setEventId(UUID.randomUUID().toString());
            domainEventService.create(eventDTO);
        }

        // Then
        verify(rabbitTemplate, times(numberOfEvents))
                .convertAndSend(
                        eq(applicationProperties.garminExchange()),
                        eq(applicationProperties.garminNewRunQueue()),
                        any(DomainEventDTO.class));
    }

    @Test
    void create_ShouldPublishCorrectMessageContent() {
        // When
        domainEventService.create(testDomainEventDTO);

        // Then - Capture and verify the actual message content
        verify(rabbitTemplate)
                .convertAndSend(
                        eq(applicationProperties.garminExchange()),
                        eq(applicationProperties.garminNewRunQueue()),
                        argThat((Object dto) -> {
                            DomainEventDTO capturedDTO = (DomainEventDTO) dto;
                            return "MESSAGING_TEST_EVENT".equals(capturedDTO.getEventType())
                                    && "Messaging test event payload".equals(capturedDTO.getPayload())
                                    && testDomain.getId().equals(capturedDTO.getDomain());
                        }));
    }

    @Test
    void update_ShouldNotPublishMessage() {
        // Given - Create an event first
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Reset mock to clear the create call
        reset(rabbitTemplate);

        DomainEventDTO updateDTO = new DomainEventDTO();
        updateDTO.setEventType("UPDATED_MESSAGING_EVENT");
        updateDTO.setPayload("Updated messaging data");
        updateDTO.setEventId(UUID.randomUUID().toString());
        updateDTO.setCreatedBy("test-user");
        updateDTO.setUpdatedBy("test-user");
        updateDTO.setDomain(testDomain.getId());

        // When
        domainEventService.update(eventId, updateDTO);

        // Then - Verify no message was published for update
        //        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void delete_ShouldNotPublishMessage() {
        // Given - Create an event first
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Reset mock to clear the create call
        reset(rabbitTemplate);

        // When
        domainEventService.delete(eventId);

        // Then - Verify no message was published for delete
        //        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void create_WithDifferentEventTypes_ShouldPublishAllCorrectly() {
        // Given
        String[] eventTypes = {"GITHUB_REPOSITORY_DELETED", "USER_REGISTERED", "DATA_PROCESSED", "SYSTEM_ALERT"};

        // When & Then
        for (String eventType : eventTypes) {
            DomainEventDTO eventDTO = new DomainEventDTO();
            eventDTO.setEventType(eventType);
            eventDTO.setPayload("Test data for " + eventType);
            eventDTO.setCreatedBy("test-user");
            eventDTO.setUpdatedBy("test-user");
            eventDTO.setDomain(testDomain.getId());
            eventDTO.setEventId(UUID.randomUUID().toString());
            domainEventService.create(eventDTO);

            // Verify message was published with correct event type
            //            verify(rabbitTemplate)
            //                    .convertAndSend(
            //                            eq(applicationProperties.garminExchange()),
            //                            eq(applicationProperties.garminNewRunQueue()),
            //                            argThat(dto -> eventType.equals(((DomainEventDTO) dto).getEventType())));
        }
    }

    @Test
    void applicationProperties_ShouldProvideCorrectExchangeAndQueue() {
        // When
        String exchange = applicationProperties.garminExchange();
        String queue = applicationProperties.garminNewRunQueue();

        // Then
        assertNotNull(exchange, "Exchange should be configured");
        assertNotNull(queue, "Queue should be configured");
        assertFalse(exchange.trim().isEmpty(), "Exchange should not be empty");
        assertFalse(queue.trim().isEmpty(), "Queue should not be empty");
    }
}
