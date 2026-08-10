package me.sathish.event_service.jobs;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.config.RabbitSchemaConfig;
import me.sathish.event_service.domain.DomainConstants;
import me.sathish.event_service.domain.DomainLookupService;
import me.sathish.event_service.domain_event.DomainEvent;
import me.sathish.event_service.domain_event.DomainEventDTO;
import me.sathish.event_service.domain_event.DomainEventMapper;
import me.sathish.event_service.domain_event.DomainEventRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ProcessRabbitMQEvents {
    private final DomainEventRepository domainEventRepository;
    private final DomainEventMapper domainEventMapper;
    private final DomainLookupService domainLookupService;
    private final java.util.Set<DomainEventDTO> processedEvents =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public ProcessRabbitMQEvents(
            DomainEventRepository domainEventRepository,
            DomainEventMapper domainEventMapper,
            DomainLookupService domainLookupService) {
        this.domainEventRepository = domainEventRepository;
        this.domainEventMapper = domainEventMapper;
        this.domainLookupService = domainLookupService;
    }

    @Transactional
    @RabbitListener(queues = RabbitSchemaConfig.GITHUB_API_EVENTS_QUEUE)
    public void processGitHubEvents(DomainEventDTO domainEventDTO) {
        //        LockAssert.assertLocked();
        if (domainEventDTO == null) {
            log.error("Received null DomainEventDTO from GitHub queue, skipping processing.");
            return;
        }

        log.info("=== Received GitHub event from RabbitMQ ===");
        log.info(
                "Received domain event: ID={}, Type={}, EventId={}",
                domainEventDTO.getId(),
                domainEventDTO.getEventType(),
                domainEventDTO.getEventId());

        try {
            final DomainEvent savedEvent = saveIncomingEvent(domainEventDTO);
            log.info(
                    "Persisted domain event payload for EventId={} as DB record ID={}",
                    domainEventDTO.getEventId(),
                    savedEvent.getId());
        } catch (RuntimeException ex) {
            log.error("Failed to persist domain event payload for EventId={}", domainEventDTO.getEventId(), ex);
            return;
        }

        processedEvents.add(domainEventDTO);
        log.info("Total processed GitHub events count from queue: {}", processedEvents.size());

        processGitHubRepoEvent(domainEventDTO);
    }

    @Transactional
    @RabbitListener(queues = RabbitSchemaConfig.GARMIN_API_EVENTS_QUEUE)
    public void processGarminEvents(Message message) {
        String eventPayload = message == null ? null : new String(message.getBody(), StandardCharsets.UTF_8);
        if (eventPayload == null || eventPayload.isBlank()) {
            log.error("Received null payload from Garmin queue, skipping processing.");
            return;
        }

        log.info("=== Received Garmin event from RabbitMQ ===");
        log.info("Event payload: {}", eventPayload);

        try {
            // Create a DomainEventDTO from the string payload
            final DomainEventDTO garminEventDTO = createDomainEventDTOFromString(eventPayload);

            // Save the incoming event to repository
            final DomainEvent savedEvent = saveIncomingEvent(garminEventDTO);
            log.info(
                    "Persisted Garmin event payload for EventId={} as DB record ID={}",
                    garminEventDTO.getEventId(),
                    savedEvent.getId());

            // Add to processed events tracking
            processedEvents.add(garminEventDTO);
            log.info("Total processed Garmin events count from queue: {}", processedEvents.size());

            // Process the Garmin event
            processGarminRunEventFromString(eventPayload);
        } catch (RuntimeException ex) {
            log.error("Failed to persist Garmin event payload: {}", eventPayload, ex);
        }
    }

    private void processGarminRunEvent(DomainEventDTO domainEventDTO) {
        log.info("Processing Garmin run event with payload: {}", domainEventDTO.getPayload());
        // Add specific Garmin run processing logic here
    }

    private void processGarminRunEventFromString(String payload) {
        log.info("Processing Garmin run event from String payload: {}", payload);

        // Add specific Garmin run processing logic here
        // You can parse the JSON payload if it's a GarminRunEvent object
    }

    private void processGitHubRepoEvent(DomainEventDTO domainEventDTO) {
        log.info("Processing GitHub deletion event with payload: {}", domainEventDTO.getPayload());
        // Add specific GitHub deletion processing logic here
    }

    private DomainEvent saveIncomingEvent(DomainEventDTO domainEventDTO) {
        final DomainEvent domainEvent = domainEventMapper.updateDomainEvent(domainEventDTO, new DomainEvent());
        return domainEventRepository.save(domainEvent);
    }

    @RabbitListener(queues = RabbitSchemaConfig.DLQ_GARMIN_API_EVENTS_QUEUE)
    public void processGarminDlqEvents(Message message) {
        String payload = message == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        // x-death is a list of maps added by RabbitMQ, one entry per death cycle
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> xDeath = (List<Map<String, Object>>) headers.get("x-death");
        String reason = "unknown";
        String sourceQueue = "unknown";
        long deathCount = 1;
        if (xDeath != null && !xDeath.isEmpty()) {
            Map<String, Object> firstDeath = xDeath.get(0);
            reason = String.valueOf(firstDeath.getOrDefault("reason", "unknown"));
            sourceQueue = String.valueOf(firstDeath.getOrDefault("queue", "unknown"));
            deathCount = (long) firstDeath.getOrDefault("count", 1L);
        }

        log.error(
            "DEAD_LETTER_ALERT [garmin-api] reason={} source={} deaths={} payload={}",
            reason, sourceQueue, deathCount, payload);
    }

    @RabbitListener(queues = RabbitSchemaConfig.DLQ_GITHUB_API_EVENTS_QUEUE)
    public void processGithubDlqEvents(Message message) {
        String payload = message == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
        log.error("DEAD_LETTER_ALERT [github-api] payload={}", payload);
    }

    private DomainEventDTO createDomainEventDTOFromString(String payload) {
        final DomainEventDTO domainEventDTO = new DomainEventDTO();
        domainEventDTO.setPayload(payload);
        domainEventDTO.setEventType("GARMIN");
        domainEventDTO.setEventId(java.util.UUID.randomUUID().toString());
        domainEventDTO.setCreatedBy("GARMIN_SERVICE");
        domainEventDTO.setUpdatedBy("GARMIN_SERVICE");
        domainEventDTO.setDomain(domainLookupService
                .ensureActiveDomain(DomainConstants.RUNS_DOMAIN)
                .getId());
        return domainEventDTO;
    }
}
