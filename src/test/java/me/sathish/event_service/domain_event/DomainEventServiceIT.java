package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import me.sathish.event_service.config.BaseIT;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.util.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/data/clearAll.sql")
class DomainEventServiceIT extends BaseIT {

    @Autowired
    private DomainEventService domainEventService;

    private Domain testDomain;
    private DomainEventDTO testDomainEventDTO;

    @BeforeEach
    void setUp() {
        // Create a test domain first
        testDomain = new Domain();
        testDomain.setDomainName("Integration Test Domain");
        testDomain.setStatus("ACTIVE");
        testDomain.setDateCreated(OffsetDateTime.now());
        testDomain = domainRepository.save(testDomain);

        // Create test DTO
        testDomainEventDTO = new DomainEventDTO();
        testDomainEventDTO.setEventType("INTEGRATION_TEST_EVENT");
        testDomainEventDTO.setPayload("Integration test event payload");
        testDomainEventDTO.setCreatedBy("test-user");
        testDomainEventDTO.setUpdatedBy("test-user");
        testDomainEventDTO.setDomain(testDomain.getId());
        testDomainEventDTO.setEventId(UUID.randomUUID().toString());
    }

    @Test
    void findAll_WhenNoEvents_ShouldReturnEmptyList() {
        // When
        List<DomainEventDTO> result = domainEventService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_WhenEventsExist_ShouldReturnAllEvents() {
        // Given
        Long eventId1 = domainEventService.create(testDomainEventDTO);

        DomainEventDTO secondEventDTO = new DomainEventDTO();
        secondEventDTO.setEventType("SECOND_TEST_EVENT");
        secondEventDTO.setPayload("Second test event payload");
        secondEventDTO.setEventId(UUID.randomUUID().toString());
        secondEventDTO.setCreatedBy("test-user");
        secondEventDTO.setUpdatedBy("test-user");
        secondEventDTO.setEventId(UUID.randomUUID().toString());
        secondEventDTO.setDomain(testDomain.getId());
        Long eventId2 = domainEventService.create(secondEventDTO);

        // When
        List<DomainEventDTO> result = domainEventService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify events are sorted by ID
        assertTrue(result.get(0).getId() <= result.get(1).getId());
    }

    @Test
    void createAndGet_ShouldPersistAndRetrieveEvent() {
        // When - Create
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Then - Verify creation
        assertNotNull(eventId);
        assertTrue(eventId > 0);

        // When - Get
        DomainEventDTO retrievedEvent = domainEventService.get(eventId);

        // Then - Verify retrieval
        assertNotNull(retrievedEvent);
        assertEquals(eventId, retrievedEvent.getId());
        assertEquals(testDomainEventDTO.getEventType(), retrievedEvent.getEventType());
        assertEquals(testDomainEventDTO.getPayload(), retrievedEvent.getPayload());
        assertEquals(testDomainEventDTO.getDomain(), retrievedEvent.getDomain());
    }

    @Test
    void get_WithInvalidId_ShouldThrowNotFoundException() {
        // Given
        Long invalidId = 999L;

        // When & Then
        assertThrows(NotFoundException.class, () -> domainEventService.get(invalidId));
    }

    @Test
    void update_ShouldModifyExistingEvent() {
        // Given
        Long eventId = domainEventService.create(testDomainEventDTO);

        DomainEventDTO updateDTO = new DomainEventDTO();
        updateDTO.setEventType("UPDATED_EVENT_TYPE");
        updateDTO.setPayload("Updated event payload");
        updateDTO.setEventId(UUID.randomUUID().toString());
        updateDTO.setCreatedBy("test-user");
        updateDTO.setUpdatedBy("test-user");
        updateDTO.setDomain(testDomain.getId());

        // When
        domainEventService.update(eventId, updateDTO);

        // Then
        DomainEventDTO updatedEvent = domainEventService.get(eventId);
        assertEquals("UPDATED_EVENT_TYPE", updatedEvent.getEventType());
        assertEquals("Updated event payload", updatedEvent.getPayload());
        assertEquals(testDomain.getId(), updatedEvent.getDomain());
    }

    @Test
    void update_WithInvalidId_ShouldThrowNotFoundException() {
        // Given
        Long invalidId = 999L;
        DomainEventDTO updateDTO = new DomainEventDTO();
        updateDTO.setEventType("UPDATED_EVENT_TYPE");
        updateDTO.setPayload("Updated event payload");
        updateDTO.setCreatedBy("test-user");
        updateDTO.setUpdatedBy("test-user");
        updateDTO.setDomain(testDomain.getId());

        // When & Then
        assertThrows(NotFoundException.class, () -> domainEventService.update(invalidId, updateDTO));
    }

    @Test
    void delete_ShouldRemoveEvent() {
        // Given
        Long eventId = domainEventService.create(testDomainEventDTO);

        // Verify event exists
        assertDoesNotThrow(() -> domainEventService.get(eventId));

        // When
        domainEventService.delete(eventId);

        // Then
        assertThrows(NotFoundException.class, () -> domainEventService.get(eventId));
    }

    @Test
    void createMultipleEvents_ShouldAllPersist() {
        // Given
        int numberOfEvents = 5;

        // When
        for (int i = 0; i < numberOfEvents; i++) {
            DomainEventDTO eventDTO = new DomainEventDTO();
            eventDTO.setEventType("BULK_TEST_EVENT_" + i);
            eventDTO.setPayload("Bulk test event payload " + i);
            eventDTO.setCreatedBy("test-user");
            eventDTO.setEventId(UUID.randomUUID().toString());
            eventDTO.setUpdatedBy("test-user");
            eventDTO.setDomain(testDomain.getId());
            domainEventService.create(eventDTO);
        }

        // Then
        List<DomainEventDTO> allEvents = domainEventService.findAll();
        assertEquals(numberOfEvents, allEvents.size());

        // Verify each event has unique data
        for (int i = 0; i < numberOfEvents; i++) {
            int finalI = i;
            assertTrue(allEvents.stream().anyMatch(event -> event.getEventType().equals("BULK_TEST_EVENT_" + finalI)));
        }
    }

    @Test
    void eventLifecycle_CreateUpdateDelete_ShouldWorkCorrectly() {
        // Create
        Long eventId = domainEventService.create(testDomainEventDTO);
        assertNotNull(eventId);

        // Read
        DomainEventDTO createdEvent = domainEventService.get(eventId);
        assertEquals(testDomainEventDTO.getEventType(), createdEvent.getEventType());

        // Update
        DomainEventDTO updateDTO = new DomainEventDTO();
        updateDTO.setEventType("LIFECYCLE_UPDATED");
        updateDTO.setPayload("Lifecycle updated payload");
        updateDTO.setEventId(UUID.randomUUID().toString());
        updateDTO.setCreatedBy("test-user");
        updateDTO.setUpdatedBy("test-user");
        updateDTO.setDomain(testDomain.getId());

        domainEventService.update(eventId, updateDTO);

        DomainEventDTO updatedEvent = domainEventService.get(eventId);
        assertEquals("LIFECYCLE_UPDATED", updatedEvent.getEventType());

        // Delete
        domainEventService.delete(eventId);
        assertThrows(NotFoundException.class, () -> domainEventService.get(eventId));
    }
}
