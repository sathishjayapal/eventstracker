package me.sathish.event_service.domain_event;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.util.ApplicationProperties;
import me.sathish.event_service.util.NotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DomainEventService {

    private final DomainEventRepository domainEventRepository;
    private final DomainRepository domainRepository;
    private final DomainEventMapper domainEventMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties applicationProperties;

    public DomainEventService(
            final DomainEventRepository domainEventRepository,
            final DomainRepository domainRepository,
            final DomainEventMapper domainEventMapper,
            final RabbitTemplate rabbitTemplate,
            final ApplicationProperties applicationProperties) {
        this.domainEventRepository = domainEventRepository;
        this.domainRepository = domainRepository;
        this.domainEventMapper = domainEventMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.applicationProperties = applicationProperties;
    }

    public List<DomainEventDTO> findAll() {
        final List<DomainEvent> domainEvents = domainEventRepository.findAll(Sort.by("id"));
        return domainEvents.stream()
                .map(domainEvent -> domainEventMapper.updateDomainEventDTO(domainEvent, new DomainEventDTO()))
                .toList();
    }

    public DomainEventDTO get(final Long id) {
        return domainEventRepository
                .findById(id)
                .map(domainEvent -> domainEventMapper.updateDomainEventDTO(domainEvent, new DomainEventDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final DomainEventDTO domainEventDTO) throws Exception {
        final DomainEvent domainEvent = new DomainEvent();
        domainEventMapper.updateDomainEvent(domainEventDTO, domainEvent, domainRepository);
        final DomainEvent savedEvent = domainEventRepository.save(domainEvent);
        // Publish message to RabbitMQ after successful save
        publishDomainEventMessage(domainEventDTO);
        return savedEvent.getId();
    }

    public void update(final Long id, final DomainEventDTO domainEventDTO) {
        final DomainEvent domainEvent = domainEventRepository.findById(id).orElseThrow(NotFoundException::new);
        domainEventMapper.updateDomainEvent(domainEventDTO, domainEvent, domainRepository);
        domainEventRepository.save(domainEvent);
    }

    public void delete(final Long id) {
        domainEventRepository.deleteById(id);
    }

    private void publishDomainEventMessage(final DomainEventDTO domainEventDTO) throws Exception {
        try {
            log.error("Exchanging domain event message to RabbitMQ" + applicationProperties.sathishProjectEventsExchange());
            for (int i = 0; i < 10; i++) {
                rabbitTemplate.convertAndSend(
                        applicationProperties.sathishProjectEventsExchange(), applicationProperties.githubRoutingKey(), domainEventDTO);
            }

        } catch (Exception e) {
            log.error("Failed to publish domain event message: " + e.getMessage());
            throw new RuntimeException("Failed to publish domain event message", e);
        }
    }
}
