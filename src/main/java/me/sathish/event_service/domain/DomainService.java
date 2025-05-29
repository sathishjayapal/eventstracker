package me.sathish.event_service.domain;

import java.util.List;
import me.sathish.event_service.domain_event.DomainEvent;
import me.sathish.event_service.domain_event.DomainEventRepository;
import me.sathish.event_service.util.NotFoundException;
import me.sathish.event_service.util.ReferencedWarning;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class DomainService {

    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;
    private final DomainEventRepository domainEventRepository;

    public DomainService(final DomainRepository domainRepository, final DomainMapper domainMapper,
            final DomainEventRepository domainEventRepository) {
        this.domainRepository = domainRepository;
        this.domainMapper = domainMapper;
        this.domainEventRepository = domainEventRepository;
    }

    public List<DomainDTO> findAll() {
        final List<Domain> domains = domainRepository.findAll(Sort.by("id"));
        return domains.stream()
                .map(domain -> domainMapper.updateDomainDTO(domain, new DomainDTO()))
                .toList();
    }

    public DomainDTO get(final Long id) {
        return domainRepository.findById(id)
                .map(domain -> domainMapper.updateDomainDTO(domain, new DomainDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final DomainDTO domainDTO) {
        final Domain domain = new Domain();
        domainMapper.updateDomain(domainDTO, domain);
        return domainRepository.save(domain).getId();
    }

    public void update(final Long id, final DomainDTO domainDTO) {
        final Domain domain = domainRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        domainMapper.updateDomain(domainDTO, domain);
        domainRepository.save(domain);
    }

    public void delete(final Long id) {
        domainRepository.deleteById(id);
    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Domain domain = domainRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final DomainEvent domainDomainEvent = domainEventRepository.findFirstByDomain(domain);
        if (domainDomainEvent != null) {
            referencedWarning.setKey("domain.domainEvent.domain.referenced");
            referencedWarning.addParam(domainDomainEvent.getId());
            return referencedWarning;
        }
        return null;
    }

}
