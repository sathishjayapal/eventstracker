package me.sathish.event_service.domain;

public interface DomainMapper {

    DomainDTO updateDomainDTO(Domain domain, DomainDTO domainDTO);

    Domain updateDomain(DomainDTO domainDTO, Domain domain);
}
