package me.sathish.event_service.jobs;

import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.domain_event.DomainEventDTO;
import me.sathish.event_service.util.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ProcessRabbitMQEvents {
    private final ApplicationProperties applicationProperties;
    private final java.util.Set<DomainEventDTO> processedEvents =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public ProcessRabbitMQEvents(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Transactional
    @RabbitListener(queues = "x.github.operations")
    public void processRunEvents(DomainEventDTO domainEventDTO) {

        //        LockAssert.assertLocked();
        if (domainEventDTO == null) {
            log.error("Received null DomainEventDTO, skipping processing.");
            return;
        }

        log.error(
                "Received domain event: ID={}, Type={}, EventId={}",
                domainEventDTO.getId(),
                domainEventDTO.getEventType(),
                domainEventDTO.getEventId());

        processedEvents.add(domainEventDTO);
        log.error("Total processed events count from queue: {}", processedEvents.size());

        // Add your event processing logic here based on event type
        switch (domainEventDTO.getEventType()) {
            case "GARMIN_RUN":
                processGarminRunEvent(domainEventDTO);
                break;
            case "GITHUB_REPOSITORY_EVENTS":
                processGitHubRepoEvent(domainEventDTO);
                break;
            default:
                log.error("Processing generic domain event of type: {}", domainEventDTO.getEventType());
        }
    }

    private void processGarminRunEvent(DomainEventDTO domainEventDTO) {
        log.error("Processing Garmin run event with payload: {}", domainEventDTO.getPayload());
        // Add specific Garmin run processing logic here
    }

    private void processGitHubRepoEvent(DomainEventDTO domainEventDTO) {
        log.error("Processing GitHub deletion event with payload: {}", domainEventDTO.getPayload());
        // Add specific GitHub deletion processing logic here
    }
}
