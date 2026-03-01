package me.sathish.event_service.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    Optional<Domain> findByDomainNameIgnoreCase(String domainName);
}
