package com.sathish.runevents.events.repo;

import com.sathish.runevents.events.data.DomainEvent;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface DomainEventRepo
        extends BaseJpaRepository<DomainEvent, Long>, ListPagingAndSortingRepository<DomainEvent, Long> {}
