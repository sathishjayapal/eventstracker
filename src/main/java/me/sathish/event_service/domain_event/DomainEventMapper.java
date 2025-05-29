package me.sathish.event_service.domain_event;

import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.util.NotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DomainEventMapper {

    @Mapping(target = "domain", ignore = true)
    DomainEventDTO updateDomainEventDTO(DomainEvent domainEvent,
            @MappingTarget DomainEventDTO domainEventDTO);

    @AfterMapping
    default void afterUpdateDomainEventDTO(DomainEvent domainEvent,
            @MappingTarget DomainEventDTO domainEventDTO) {
        domainEventDTO.setDomain(domainEvent.getDomain() == null ? null : domainEvent.getDomain().getId());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "domain", ignore = true)
    DomainEvent updateDomainEvent(DomainEventDTO domainEventDTO,
            @MappingTarget DomainEvent domainEvent, @Context DomainRepository domainRepository);

    @AfterMapping
    default void afterUpdateDomainEvent(DomainEventDTO domainEventDTO,
            @MappingTarget DomainEvent domainEvent, @Context DomainRepository domainRepository) {
        final Domain domain = domainEventDTO.getDomain() == null ? null : domainRepository.findById(domainEventDTO.getDomain())
                .orElseThrow(() -> new NotFoundException("domain not found"));
        domainEvent.setDomain(domain);
    }

}
