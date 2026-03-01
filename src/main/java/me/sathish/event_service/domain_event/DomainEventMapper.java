package me.sathish.event_service.domain_event;

public interface DomainEventMapper {

    DomainEventDTO updateDomainEventDTO(DomainEvent domainEvent, DomainEventDTO domainEventDTO);

    DomainEvent updateDomainEvent(DomainEventDTO domainEventDTO, DomainEvent domainEvent);
}
