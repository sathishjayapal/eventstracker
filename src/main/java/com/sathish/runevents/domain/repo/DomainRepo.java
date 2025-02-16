package com.sathish.runevents.domain.repo;

import com.sathish.runevents.domain.data.Domain;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import java.util.Optional;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface DomainRepo extends BaseJpaRepository<Domain, Long>, ListPagingAndSortingRepository<Domain, Long> {
    Optional<Domain> findByDomainName(String domainName);
}
