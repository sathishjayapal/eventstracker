package me.sathish.event_service;

import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.event_domain_user.EventDomainUser;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(1)
public class EventDomainUserDataLoader implements ApplicationRunner {
    private final EventDomainUserRepository eventDomainUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public EventDomainUserDataLoader(
            EventDomainUserRepository eventDomainUserRepository,
            PasswordEncoder passwordEncoder,
            Environment environment) {
        this.eventDomainUserRepository = eventDomainUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String username = environment.getProperty("eventDomainUser");
        String password = environment.getProperty("eventDomainUserPassword");
        if (username == null || password == null) {
            throw new IllegalStateException(
                    "eventDomainUser and eventDomainUserPassword properties must not be null");
        }
        createIfAbsent(username, password);
        createIfAbsent(username + "admin", password);
    }

    private void createIfAbsent(String username, String password) {
        if (eventDomainUserRepository.findByUsernameIgnoreCase(username) != null) {
            log.info("Event Domain User '{}' already exists, skipping", username);
            return;
        }
        EventDomainUser user = new EventDomainUser();
        user.setUsername(username);
        user.setHash(passwordEncoder.encode(password));
        eventDomainUserRepository.saveAndFlush(user);
        log.info("Created Event Domain User '{}'", username);
    }
}
