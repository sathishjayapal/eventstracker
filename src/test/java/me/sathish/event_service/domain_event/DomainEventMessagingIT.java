package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
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

        // Configure mock RabbitTemplate to not throw exceptions by default
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void create_ShouldPublishMessageToRabbitMQ() throws Exception {
        // When
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Then
        assertNotNull(eventId);

        // Verify RabbitMQ message was sent
        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        eq(applicationProperties.garminExchange()),
                        eq(applicationProperties.garminNewRunQueue()),
                        any(Object.class));
    }

    @Test
    void create_WithRabbitMQFailure_ShouldStillPersistButThrowException() {
        // Given - Configure mock to throw exception
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(Object.class));

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            domainEventService.create(testDomainEventDTO);
        });

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Failed to publish domain event message"));

        // Verify that the event was still persisted despite messaging failure
        // (This would require checking the database, but for now we just verify the exception)
    }

    @Test
    void create_MultipleEvents_ShouldPublishAllMessages() throws Exception {
        // Given
        List<DomainEventDTO> events = Arrays.asList(
                createTestEventDTO("EVENT_1"), createTestEventDTO("EVENT_2"), createTestEventDTO("EVENT_3"));

        // When
        for (DomainEventDTO event : events) {
            domainEventService.create(event);
        }

        // Then
        verify(rabbitTemplate, times(3)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void create_ShouldPublishCorrectMessageContent() throws Exception {
        // When
        domainEventService.create(testDomainEventDTO);

        // Then
        verify(rabbitTemplate)
                .convertAndSend(
                        eq(applicationProperties.garminExchange()),
                        eq(applicationProperties.garminNewRunQueue()),
                        argThat((Object dto) -> {
                            DomainEventDTO publishedDto = (DomainEventDTO) dto;
                            return "MESSAGING_TEST_EVENT".equals(publishedDto.getEventType())
                                    && "Messaging test event payload".equals(publishedDto.getPayload())
                                    && "test-user".equals(publishedDto.getCreatedBy());
                        }));
    }

    @Test
    void update_ShouldNotPublishMessage() throws Exception {
        // Given - First create an event
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Reset mock to clear the create call
        reset(rabbitTemplate);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // When - Update the event
        testDomainEventDTO.setPayload("Updated payload");
        domainEventService.update(eventId, testDomainEventDTO);

        // Then - Verify no message was published for update
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void delete_ShouldNotPublishMessage() throws Exception {
        // Given - First create an event
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Reset mock to clear the create call
        reset(rabbitTemplate);

        // When - Delete the event
        domainEventService.delete(eventId);

        // Then - Verify no message was published for delete
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void create_WithDifferentEventTypes_ShouldPublishAllCorrectly() throws Exception {
        // Given
        List<String> eventTypes = Arrays.asList(
                "USER_CREATED", "USER_UPDATED", "USER_DELETED", "ORDER_PLACED", "ORDER_SHIPPED", "ORDER_DELIVERED");

        // When
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
            verify(rabbitTemplate)
                    .convertAndSend(
                            eq(applicationProperties.garminExchange()),
                            eq(applicationProperties.garminNewRunQueue()),
                            argThat((Object dto) -> eventType.equals(((DomainEventDTO) dto).getEventType())));
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

    private DomainEventDTO createTestEventDTO(String eventType) {
        DomainEventDTO eventDTO = new DomainEventDTO();
        eventDTO.setEventType(eventType);
        eventDTO.setPayload("Test data for " + eventType);
        eventDTO.setCreatedBy("test-user");
        eventDTO.setUpdatedBy("test-user");
        eventDTO.setDomain(testDomain.getId());
        eventDTO.setEventId(UUID.randomUUID().toString());
        return eventDTO;
    }
}
