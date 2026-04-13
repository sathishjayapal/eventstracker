package me.sathish.event_service.domain_event;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainConstants;
import me.sathish.event_service.domain.DomainLookupService;
import me.sathish.event_service.util.ApplicationProperties;
import me.sathish.event_service.util.NotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DomainEventService {

    private final DomainEventRepository domainEventRepository;
    private final DomainLookupService domainLookupService;
    private final DomainEventMapper domainEventMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties applicationProperties;

    public DomainEventService(
            final DomainEventRepository domainEventRepository,
            final DomainLookupService domainLookupService,
            final DomainEventMapper domainEventMapper,
            final RabbitTemplate rabbitTemplate,
            final ApplicationProperties applicationProperties) {
        this.domainEventRepository = domainEventRepository;
        this.domainLookupService = domainLookupService;
        this.domainEventMapper = domainEventMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.applicationProperties = applicationProperties;
    }

    public List<DomainEventDTO> findAll() {
        Pageable page = PageRequest.of(0, 10);
        final List<DomainEvent> domainEvents =
                domainEventRepository.findAll(page).getContent();
        //        final List<DomainEvent> domainEvents = domainEventRepository.findAll(Sort.by("id"));

        return domainEvents.stream()
                .map(this::toDto)
                .toList();
    }

    public org.springframework.data.domain.Page<DomainEventDTO> findAllPaged(Pageable pageable) {
        return domainEventRepository.findAll(pageable)
                .map(this::toDto);
    }

    public DomainEventDTO get(final Long id) {
        return domainEventRepository
                .findById(id)
                .map(this::toDto)
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final DomainEventDTO domainEventDTO) throws Exception {
        final String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        domainEventDTO.setCreatedBy(currentUser);
        domainEventDTO.setUpdatedBy(currentUser);
        final DomainEvent domainEvent = new DomainEvent();
        final var domain = resolveAndPopulateDomain(domainEventDTO);
        domainEventMapper.updateDomainEvent(domainEventDTO, domainEvent);
        domainEvent.setDomain(domain);
        final DomainEvent savedEvent = domainEventRepository.save(domainEvent);
        // Publish message to RabbitMQ after successful save
        publishDomainEventMessage(domainEventDTO);
        return savedEvent.getId();
    }

    public void update(final Long id, final DomainEventDTO domainEventDTO) {
        final String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        domainEventDTO.setUpdatedBy(currentUser);
        final DomainEvent domainEvent = domainEventRepository.findById(id).orElseThrow(NotFoundException::new);
        domainEventDTO.setCreatedBy(domainEvent.getCreatedBy());
        final var domain = resolveAndPopulateDomain(domainEventDTO);
        domainEventMapper.updateDomainEvent(domainEventDTO, domainEvent);
        domainEvent.setDomain(domain);
        domainEventRepository.save(domainEvent);
    }

    public void delete(final Long id) {
        domainEventRepository.deleteById(id);
    }

    private void publishDomainEventMessage(final DomainEventDTO domainEventDTO) throws Exception {
        try {
            log.error("Exchanging domain event message to RabbitMQ"
                    + applicationProperties.sathishProjectEventsExchange());
            for (int i = 0; i < 10; i++) {
                rabbitTemplate.convertAndSend(
                        applicationProperties.sathishProjectEventsExchange(),
                        applicationProperties.githubRoutingKey(),
                        domainEventDTO);
            }

        } catch (Exception e) {
            log.error("Failed to publish domain event message: " + e.getMessage());
            throw new RuntimeException("Failed to publish domain event message", e);
        }
    }

    private DomainEventDTO toDto(DomainEvent domainEvent) {
        return domainEventMapper.updateDomainEventDTO(domainEvent, new DomainEventDTO());
    }

    private Domain resolveAndPopulateDomain(DomainEventDTO domainEventDTO) {
        if (domainEventDTO.getDomain() != null) {
            final var domain = domainLookupService.ensureActiveDomain(domainEventDTO.getDomain());
            domainEventDTO.setDomain(domain.getId());
            domainEventDTO.setDomainName(domain.getDomainName());
            return domain;
        } else if (domainEventDTO.getDomainName() != null) {
            final var domain = domainLookupService.ensureActiveDomain(domainEventDTO.getDomainName());
            domainEventDTO.setDomain(domain.getId());
            domainEventDTO.setDomainName(domain.getDomainName());
            return domain;
        }
        return null;
    }
}
