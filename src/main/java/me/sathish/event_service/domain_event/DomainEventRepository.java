package me.sathish.event_service.domain_event;

import me.sathish.event_service.domain.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainEventRepository extends JpaRepository<DomainEvent, Long> {

    DomainEvent findFirstByDomain(Domain domain);
}
