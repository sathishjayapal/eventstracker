package me.sathish.event_service.domain;

import org.springframework.stereotype.Component;

@Component
public class DomainMapperImpl implements DomainMapper {

    @Override
    public DomainDTO updateDomainDTO(Domain domain, DomainDTO domainDTO) {
        if (domain == null) {
            return domainDTO;
        }

        domainDTO.setId(domain.getId());
        domainDTO.setDomainName(domain.getDomainName());
        domainDTO.setStatus(domain.getStatus());
        domainDTO.setComments(domain.getComments());

        return domainDTO;
    }

    @Override
    public Domain updateDomain(DomainDTO domainDTO, Domain domain) {
        if (domainDTO == null) {
            return domain;
        }

        domain.setDomainName(domainDTO.getDomainName());
        domain.setStatus(domainDTO.getStatus());
        domain.setComments(domainDTO.getComments());

        return domain;
    }
}
