package me.sathish.event_service.domain_event;

import java.util.List;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class DomainEventService {

    private final DomainEventRepository domainEventRepository;
    private final DomainRepository domainRepository;
    private final DomainEventMapper domainEventMapper;

    public DomainEventService(final DomainEventRepository domainEventRepository,
            final DomainRepository domainRepository, final DomainEventMapper domainEventMapper) {
        this.domainEventRepository = domainEventRepository;
        this.domainRepository = domainRepository;
        this.domainEventMapper = domainEventMapper;
    }

    public List<DomainEventDTO> findAll() {
        final List<DomainEvent> domainEvents = domainEventRepository.findAll(Sort.by("id"));
        return domainEvents.stream()
                .map(domainEvent -> domainEventMapper.updateDomainEventDTO(domainEvent, new DomainEventDTO()))
                .toList();
    }

    public DomainEventDTO get(final Long id) {
        return domainEventRepository.findById(id)
                .map(domainEvent -> domainEventMapper.updateDomainEventDTO(domainEvent, new DomainEventDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final DomainEventDTO domainEventDTO) {
        final DomainEvent domainEvent = new DomainEvent();
        domainEventMapper.updateDomainEvent(domainEventDTO, domainEvent, domainRepository);
        return domainEventRepository.save(domainEvent).getId();
    }

    public void update(final Long id, final DomainEventDTO domainEventDTO) {
        final DomainEvent domainEvent = domainEventRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        domainEventMapper.updateDomainEvent(domainEventDTO, domainEvent, domainRepository);
        domainEventRepository.save(domainEvent);
    }

    public void delete(final Long id) {
        domainEventRepository.deleteById(id);
    }

}
