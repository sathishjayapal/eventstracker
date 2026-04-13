package me.sathish.event_service.domain_event;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainLookupService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventMapperImpl implements DomainEventMapper {
    private static final ZoneId CST_ZONE = ZoneId.of("America/Chicago");
    private static final DateTimeFormatter DATETIME_LOCAL_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(CST_ZONE);
    private final DomainLookupService domainLookupService;

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
        if (domainEvent.getDateCreated() != null) {
            domainEventDTO.setDateCreated(domainEvent.getDateCreated().format(DATETIME_LOCAL_FORMAT));
        }
        if (domainEvent.getLastUpdated() != null) {
            domainEventDTO.setLastUpdated(domainEvent.getLastUpdated().format(DATETIME_LOCAL_FORMAT));
        }
        if (domainEvent.getDomain() != null) {
            final Domain domainProxy = domainEvent.getDomain();
            // id is available from proxy without initialization
            domainEventDTO.setDomain(domainProxy.getId());
            if (Hibernate.isInitialized(domainProxy)) {
                domainEventDTO.setDomainName(domainProxy.getDomainName());
            } else {
                final Domain initializedDomain = domainLookupService.getDomain(domainProxy.getId());
                domainEventDTO.setDomainName(initializedDomain.getDomainName());
            }
        } else {
            domainEventDTO.setDomain(null);
            domainEventDTO.setDomainName(null);
        }

        return domainEventDTO;
    }

    @Override
    public DomainEvent updateDomainEvent(DomainEventDTO domainEventDTO, DomainEvent domainEvent) {
        if (domainEventDTO == null) {
            return domainEvent;
        }

        domainEvent.setEventId(domainEventDTO.getEventId());
        domainEvent.setEventType(domainEventDTO.getEventType());
        domainEvent.setPayload(domainEventDTO.getPayload());
        domainEvent.setCreatedBy(domainEventDTO.getCreatedBy());
        domainEvent.setUpdatedBy(domainEventDTO.getUpdatedBy());

        Domain domain = domainEventDTO.getDomain() == null
                ? null
                : domainLookupService.ensureActiveDomain(domainEventDTO.getDomain());
        domainEvent.setDomain(domain);

        return domainEvent;
    }
}
