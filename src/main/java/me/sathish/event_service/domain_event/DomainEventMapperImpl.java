package me.sathish.event_service.domain_event;

import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.util.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class DomainEventMapperImpl implements DomainEventMapper {

    @Override
    public DomainEventDTO updateDomainEventDTO(DomainEvent domainEvent, DomainEventDTO domainEventDTO) {
        if (domainEvent == null) {
            return domainEventDTO;
        }

        domainEventDTO.setId(domainEvent.getId());
        domainEventDTO.setEventId(domainEvent.getEventId());
        domainEventDTO.setEventType(domainEvent.getEventType());
        domainEventDTO.setPayload(domainEvent.getPayload());
        domainEventDTO.setCreatedBy(domainEvent.getCreatedBy());
        domainEventDTO.setUpdatedBy(domainEvent.getUpdatedBy());
        domainEventDTO.setDomain(domainEvent.getDomain() == null ? null : domainEvent.getDomain().getId());

        return domainEventDTO;
    }

    @Override
    public DomainEvent updateDomainEvent(
            DomainEventDTO domainEventDTO,
            DomainEvent domainEvent,
            DomainRepository domainRepository) {
        if (domainEventDTO == null) {
            return domainEvent;
        }

        domainEvent.setEventId(domainEventDTO.getEventId());
        domainEvent.setEventType(domainEventDTO.getEventType());
        domainEvent.setPayload(domainEventDTO.getPayload());
        domainEvent.setCreatedBy(domainEventDTO.getCreatedBy());
        domainEvent.setUpdatedBy(domainEventDTO.getUpdatedBy());

        final Domain domain = domainEventDTO.getDomain() == null
                ? null
                : domainRepository
                        .findById(domainEventDTO.getDomain())
                        .orElseThrow(() -> new NotFoundException("domain not found"));
        domainEvent.setDomain(domain);

        return domainEvent;
    }
}
