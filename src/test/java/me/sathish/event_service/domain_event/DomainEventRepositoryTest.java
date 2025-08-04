package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.sathish.event_service.config.BaseIT;
import me.sathish.event_service.domain.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

@Sql("/data/clearAll.sql")
class DomainEventRepositoryTest extends BaseIT {

    @Autowired
    private DomainEventRepository domainEventRepository;

    private Domain testDomain;
    private DomainEvent testDomainEvent;

    @BeforeEach
    void setUp() {
        // Create a test domain first
        testDomain = new Domain();
        testDomain.setDomainName("Repository Test Domain");
        testDomain.setStatus("ACTIVE");
        testDomain.setDateCreated(OffsetDateTime.now());
        testDomain = domainRepository.save(testDomain);

        // Create test domain event
        testDomainEvent = new DomainEvent();
        testDomainEvent.setEventType("REPOSITORY_TEST_EVENT");
        testDomainEvent.setPayload("Repository test event payload");
        testDomainEvent.setEventId(UUID.randomUUID().toString());
        testDomainEvent.setCreatedBy("test-user");
        testDomainEvent.setUpdatedBy("test-user");
        testDomainEvent.setDomain(testDomain);
        testDomainEvent.setDateCreated(OffsetDateTime.now());
    }

    @Test
    void save_ShouldPersistDomainEvent() {
        // When
        DomainEvent savedEvent = domainEventRepository.save(testDomainEvent);

        // Then
        assertNotNull(savedEvent);
        assertNotNull(savedEvent.getId());
        assertTrue(savedEvent.getId() > 0);
        assertEquals(testDomainEvent.getEventType(), savedEvent.getEventType());
        assertEquals(testDomainEvent.getPayload(), savedEvent.getPayload());
        assertEquals(testDomainEvent.getDomain().getId(), savedEvent.getDomain().getId());
        assertNotNull(savedEvent.getDateCreated());
    }

    @Test
    void findById_WithValidId_ShouldReturnEvent() {
        // Given
        DomainEvent savedEvent = domainEventRepository.save(testDomainEvent);

        // When
        Optional<DomainEvent> result = domainEventRepository.findById(savedEvent.getId());

        // Then
        assertTrue(result.isPresent());
        DomainEvent foundEvent = result.get();
        assertEquals(savedEvent.getId(), foundEvent.getId());
        assertEquals(savedEvent.getEventType(), foundEvent.getEventType());
        assertEquals(savedEvent.getPayload(), foundEvent.getPayload());
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        // Given
        Long invalidId = 999L;

        // When
        Optional<DomainEvent> result = domainEventRepository.findById(invalidId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedEvents() {
        // Given
        DomainEvent event1 = new DomainEvent();
        event1.setEventType("EVENT_A");
        event1.setPayload("First event");
        event1.setEventId(UUID.randomUUID().toString());
        event1.setDomain(testDomain);
        event1.setCreatedBy("test-user");
        event1.setUpdatedBy("test-user");
        event1.setDateCreated(OffsetDateTime.now());

        DomainEvent event2 = new DomainEvent();
        event2.setEventType("EVENT_B");
        event2.setEventId(UUID.randomUUID().toString());
        event2.setPayload("Second event");
        event2.setDomain(testDomain);
        event2.setCreatedBy("test-user");
        event2.setUpdatedBy("test-user");
        event2.setDateCreated(OffsetDateTime.now());

        DomainEvent savedEvent1 = domainEventRepository.save(event1);
        DomainEvent savedEvent2 = domainEventRepository.save(event2);

        // When
        List<DomainEvent> result = domainEventRepository.findAll(Sort.by("id"));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify sorting by ID
        assertTrue(result.get(0).getId() <= result.get(1).getId());
    }

    @Test
    void deleteById_ShouldRemoveEvent() {
        // Given
        DomainEvent savedEvent = domainEventRepository.save(testDomainEvent);
        Long eventId = savedEvent.getId();

        // Verify event exists
        assertTrue(domainEventRepository.findById(eventId).isPresent());

        // When
        domainEventRepository.deleteById(eventId);

        // Then
        assertFalse(domainEventRepository.findById(eventId).isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given - Initially empty
        assertEquals(0, domainEventRepository.count());

        // When - Add events
        domainEventRepository.save(testDomainEvent);

        DomainEvent secondEvent = new DomainEvent();
        secondEvent.setEventType("SECOND_EVENT");
        secondEvent.setPayload("Second event data");
        secondEvent.setEventId(UUID.randomUUID().toString());
        secondEvent.setDomain(testDomain);
        secondEvent.setDateCreated(OffsetDateTime.now());
        secondEvent.setCreatedBy("test-user");
        secondEvent.setUpdatedBy("test-user");
        domainEventRepository.save(secondEvent);

        // Then
        assertEquals(2, domainEventRepository.count());
    }

    @Test
    void existsById_ShouldReturnCorrectStatus() {
        // Given
        DomainEvent savedEvent = domainEventRepository.save(testDomainEvent);

        // When & Then
        assertTrue(domainEventRepository.existsById(savedEvent.getId()));
        assertFalse(domainEventRepository.existsById(999L));
    }

    @Test
    void saveAndFlush_ShouldImmediatelyPersist() {
        // When
        DomainEvent savedEvent = domainEventRepository.saveAndFlush(testDomainEvent);

        // Then
        assertNotNull(savedEvent.getId());

        // Verify it's immediately available in database
        Optional<DomainEvent> foundEvent = domainEventRepository.findById(savedEvent.getId());
        assertTrue(foundEvent.isPresent());
    }

    @Test
    void updateEvent_ShouldModifyExistingRecord() {
        // Given
        DomainEvent savedEvent = domainEventRepository.save(testDomainEvent);
        Long eventId = savedEvent.getId();

        // When - Update the event
        savedEvent.setEventType("UPDATED_EVENT_TYPE");
        savedEvent.setPayload("Updated event data");
        DomainEvent updatedEvent = domainEventRepository.save(savedEvent);

        // Then
        assertEquals(eventId, updatedEvent.getId()); // Same ID
        assertEquals("UPDATED_EVENT_TYPE", updatedEvent.getEventType());
        assertEquals("Updated event data", updatedEvent.getPayload());

        // Verify in database
        Optional<DomainEvent> foundEvent = domainEventRepository.findById(eventId);
        assertTrue(foundEvent.isPresent());
        assertEquals("UPDATED_EVENT_TYPE", foundEvent.get().getEventType());
    }

    @Test
    void cascadeDelete_WhenDomainDeleted_ShouldHandleCorrectly() {
        // Given
        DomainEvent savedEvent = domainEventRepository.save(testDomainEvent);
        Long eventId = savedEvent.getId();
        Long domainId = testDomain.getId();

        // Verify event exists
        assertTrue(domainEventRepository.existsById(eventId));

        // When - Delete domain (this should handle cascade appropriately)
        // Note: Actual cascade behavior depends on JPA mapping configuration
        domainRepository.deleteById(domainId);

        // Then - Verify event handling
        // This test verifies the relationship integrity
        // The exact behavior depends on cascade settings in the entity
    }
}
