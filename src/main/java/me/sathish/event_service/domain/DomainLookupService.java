package me.sathish.event_service.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainLookupService {

    private final DomainRepository domainRepository;

    public Domain ensureActiveDomain(String domainName) {
        Domain domain = domainRepository
                .findByDomainNameIgnoreCase(domainName)
                .orElseThrow(() -> new DomainNotFoundException(domainName));
        requireActive(domain);
        return domain;
    }

    public Domain ensureActiveDomain(Long domainId) {
        Domain domain = domainRepository.findById(domainId).orElseThrow(() -> new DomainNotFoundException(domainId));
        requireActive(domain);
        return domain;
    }

    public Domain getDomain(Long domainId) {
        return domainRepository.findById(domainId).orElseThrow(() -> new DomainNotFoundException(domainId));
    }

    private void requireActive(Domain domain) {
        if (!DomainConstants.STATUS_ACTIVE.equalsIgnoreCase(domain.getStatus())) {
            throw new DomainInactiveException(domain.getDomainName());
        }
    }
}
