package com.sathish.runevents.events.service;

import com.sathish.runevents.domain.data.Domain;
import com.sathish.runevents.domain.repo.DomainRepo;
import com.sathish.runevents.events.data.DomainEvent;
import com.sathish.runevents.events.data.DomainEventDTO;
import com.sathish.runevents.events.data.DomainEventMapper;
import com.sathish.runevents.events.repo.DomainEventRepo;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DomainEventService {
    private final DomainEventRepo domainEventRepo;
    private final DomainRepo domainRepo;
    private final DomainEventMapper domainEventMapper;

    @Autowired
    public DomainEventService(
            DomainEventRepo domainEventRepo, DomainRepo domainRepo, DomainEventMapper domainEventMapper) {
        this.domainEventRepo = domainEventRepo;
        this.domainRepo = domainRepo;
        this.domainEventMapper = domainEventMapper;
    }

    @Transactional
    public DomainEventDTO createDomainEvent(DomainEventDTO domainEventDTO) {
        Optional<Domain> domain = domainRepo.findByDomainName(domainEventDTO.getDomainName());
        if (domain.isPresent()) {
            DomainEvent domainEvent = domainEventMapper.toEntity(domainEventDTO, domain.get());
            domainEvent = domainEventRepo.persist(domainEvent);
            return domainEventMapper.toDto(domainEvent);
        } else {
            throw new IllegalArgumentException("Domain not found: " + domainEventDTO.getDomainName());
        }
    }

    public DomainEventDTO getDomainEvent(Long id) {
        Optional<DomainEvent> domainEvent = domainEventRepo.findById(id);
        return domainEvent.map(domainEventMapper::toDto).orElse(null);
    }
}
