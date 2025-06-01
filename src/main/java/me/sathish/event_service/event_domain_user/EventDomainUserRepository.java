package me.sathish.event_service.event_domain_user;

import org.springframework.data.jpa.repository.JpaRepository;


public interface EventDomainUserRepository extends JpaRepository<EventDomainUser, Long> {

    EventDomainUser findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

}
