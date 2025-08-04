package me.sathish.event_service.util;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain_event.DomainEvent;
import me.sathish.event_service.domain_event.DomainEventDTO;

/**
 * Test data builder utility class for creating test objects.
 * Provides fluent API for building test data with sensible defaults.
 */
public class TestDataBuilder {

    public static class DomainBuilder {
        private String domainName = "Test Domain";
        private String status = "ACTIVE";
        private String comments = "Test comments";
        private OffsetDateTime dateCreated = OffsetDateTime.now();

        public DomainBuilder withDomainName(String domainName) {
            this.domainName = domainName;
            return this;
        }

        public DomainBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public DomainBuilder withComments(String comments) {
            this.comments = comments;
            return this;
        }

        public DomainBuilder withDateCreated(OffsetDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Domain build() {
            Domain domain = new Domain();
            domain.setDomainName(domainName);
            domain.setStatus(status);
            domain.setComments(comments);
            domain.setDateCreated(dateCreated);
            return domain;
        }
    }

    public static class DomainEventBuilder {
        private String eventId = "test-event-id";
        private String eventType = "TEST_EVENT";
        private String payload = "Test event payload";
        private String createdBy = "test-user";
        private String updatedBy = "test-user";
        private String evnetId = UUID.randomUUID().toString();
        private Domain domain;
        private OffsetDateTime dateCreated = OffsetDateTime.now();

        public DomainEventBuilder withEventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public DomainEventBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public DomainEventBuilder withPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public DomainEventBuilder withCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public DomainEventBuilder withUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public DomainEventBuilder withDomain(Domain domain) {
            this.domain = domain;
            return this;
        }

        public DomainEventBuilder withDateCreated(OffsetDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public DomainEvent build() {
            DomainEvent domainEvent = new DomainEvent();
            domainEvent.setEventId(eventId);
            domainEvent.setEventType(eventType);
            domainEvent.setPayload(payload);
            domainEvent.setCreatedBy(createdBy);
            domainEvent.setEventId(evnetId);
            domainEvent.setUpdatedBy(updatedBy);
            domainEvent.setDomain(domain);
            domainEvent.setDateCreated(dateCreated);
            return domainEvent;
        }
    }

    public static class DomainEventDTOBuilder {
        private Long id;
        private String eventId = "test-event-id";
        private String eventType = "TEST_EVENT";
        private String payload = "Test event payload";
        private String createdBy = "test-user";
        private String updatedBy = "test-user";
        private Long domain;
        private OffsetDateTime dateCreated = OffsetDateTime.now();

        public DomainEventDTOBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public DomainEventDTOBuilder withEventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public DomainEventDTOBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public DomainEventDTOBuilder withPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public DomainEventDTOBuilder withCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public DomainEventDTOBuilder withUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public DomainEventDTOBuilder withDomain(Long domain) {
            this.domain = domain;
            return this;
        }

        public DomainEventDTOBuilder withDateCreated(OffsetDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public DomainEventDTO build() {
            DomainEventDTO dto = new DomainEventDTO();
            dto.setId(id);
            dto.setEventId(eventId);
            dto.setEventType(eventType);
            dto.setPayload(payload);
            dto.setCreatedBy(createdBy);
            dto.setUpdatedBy(updatedBy);
            dto.setDomain(domain);
            //            dto.setDateCreated(dateCreated);
            return dto;
        }

        public DomainEventDTOBuilder withEventData(String eventData) {
            this.payload = eventData; // or create a new eventData field
            return this;
        }
    }

    // Factory methods for easy access
    public static DomainBuilder domain() {
        return new DomainBuilder();
    }

    public static DomainEventBuilder domainEvent() {
        return new DomainEventBuilder();
    }

    public static DomainEventDTOBuilder domainEventDTO() {
        return new DomainEventDTOBuilder();
    }

    // Convenience methods for common test scenarios
    public static List<DomainEventDTO> createMultipleDomainEventDTOs(int count, Long domainId) {
        List<DomainEventDTO> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(domainEventDTO()
                    .withEventType("BULK_TEST_EVENT_" + i)
                    .withPayload("Bulk test data " + i)
                    .withDomain(domainId)
                    .build());
        }
        return events;
    }

    public static List<Domain> createMultipleDomains(int count) {
        List<Domain> domains = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            domains.add(domain().withDomainName("Test Domain " + i).build());
        }
        return domains;
    }

    // Common event types for testing
    public static class EventTypes {
        public static final String GITHUB_REPOSITORY_DELETED = "GITHUB_REPOSITORY_DELETED";
        public static final String USER_REGISTERED = "USER_REGISTERED";
        public static final String DATA_PROCESSED = "DATA_PROCESSED";
        public static final String SYSTEM_ALERT = "SYSTEM_ALERT";
        public static final String INTEGRATION_TEST = "INTEGRATION_TEST";
        public static final String PERFORMANCE_TEST = "PERFORMANCE_TEST";
    }

    // Pre-built test scenarios
    public static DomainEventDTO githubRepositoryDeletedEvent(Long domainId) {
        return domainEventDTO()
                .withEventType(EventTypes.GITHUB_REPOSITORY_DELETED)
                .withPayload("{\"repositoryName\":\"test-repo\",\"owner\":\"testuser\"}")
                .withDomain(domainId)
                .build();
    }

    public static DomainEventDTO userRegisteredEvent(Long domainId) {
        return domainEventDTO()
                .withEventType(EventTypes.USER_REGISTERED)
                .withPayload("{\"userId\":\"12345\",\"email\":\"test@example.com\"}")
                .withDomain(domainId)
                .build();
    }

    public static DomainEventDTO systemAlertEvent(Long domainId) {
        return domainEventDTO()
                .withEventType(EventTypes.SYSTEM_ALERT)
                .withPayload("{\"level\":\"WARNING\",\"message\":\"System resource usage high\"}")
                .withDomain(domainId)
                .build();
    }
}
