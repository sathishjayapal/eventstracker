package me.sathish.event_service.domain_event;

import me.sathish.event_service.domain.DomainRepository;

public interface DomainEventMapper {

    DomainEventDTO updateDomainEventDTO(DomainEvent domainEvent, DomainEventDTO domainEventDTO);

    DomainEvent updateDomainEvent(
            DomainEventDTO domainEventDTO,
            DomainEvent domainEvent,
            DomainRepository domainRepository);
}
