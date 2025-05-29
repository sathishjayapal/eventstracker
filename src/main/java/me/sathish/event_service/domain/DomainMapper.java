package me.sathish.event_service.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DomainMapper {

    DomainDTO updateDomainDTO(Domain domain, @MappingTarget DomainDTO domainDTO);

    @Mapping(target = "id", ignore = true)
    Domain updateDomain(DomainDTO domainDTO, @MappingTarget Domain domain);

}
