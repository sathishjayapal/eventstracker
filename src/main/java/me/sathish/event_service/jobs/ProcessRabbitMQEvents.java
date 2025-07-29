package me.sathish.event_service.jobs;

import me.sathish.event_service.domain_event.DomainEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessRabbitMQEvents {
    private static final Logger logger = LoggerFactory.getLogger(ProcessRabbitMQEvents.class);
    private final java.util.Set<DomainEventDTO> processedEvents =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    @Transactional
    @RabbitListener(queues = "garminrun-run-queue")
    //    @Scheduled(cron = "${processRunEvents.fixedRate}")

    public void processRunEvents(DomainEventDTO domainEventDTO) {

        //        LockAssert.assertLocked();
        if (domainEventDTO == null) {
            logger.warn("Received null DomainEventDTO, skipping processing.");
            return;
        }

        logger.info(
                "Received domain event: ID={}, Type={}, EventId={}",
                domainEventDTO.getId(),
                domainEventDTO.getEventType(),
                domainEventDTO.getEventId());

        processedEvents.add(domainEventDTO);
        logger.info("Total processed events count: {}", processedEvents.size());

        // Add your event processing logic here based on event type
        switch (domainEventDTO.getEventType()) {
            case "GARMIN_RUN":
                processGarminRunEvent(domainEventDTO);
                break;
            case "GITHUB_REPOSITORY_EVENTS":
                processGitHubRepoEvent(domainEventDTO);
                break;
            default:
                logger.info("Processing generic domain event of type: {}", domainEventDTO.getEventType());
        }
    }

    private void processGarminRunEvent(DomainEventDTO domainEventDTO) {
        logger.info("Processing Garmin run event with payload: {}", domainEventDTO.getPayload());
        // Add specific Garmin run processing logic here
    }

    private void processGitHubRepoEvent(DomainEventDTO domainEventDTO) {
        logger.info("Processing GitHub deletion event with payload: {}", domainEventDTO.getPayload());
        // Add specific GitHub deletion processing logic here
    }
}
