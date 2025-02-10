package com.sathish.runevents.domain.repo;

import com.sathish.runevents.domain.data.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DomainRepo extends JpaRepository<Domain, Long> {
    Optional<Domain> findByDomainName(String domainName);
}
