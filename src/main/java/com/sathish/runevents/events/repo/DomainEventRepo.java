package com.sathish.runevents.events.repo;

import com.sathish.runevents.events.data.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainEventRepo extends JpaRepository<DomainEvent, Long> {
}
