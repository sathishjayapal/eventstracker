package me.sathish.event_service;

import me.sathish.event_service.event_domain_user.EventDomainUser;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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
        if (eventDomainUserRepository.findByUsernameIgnoreCase(username) != null) {
            System.out.println("Event Domain User already exists");
        } else {
            if (username == null || password == null) {
                throw new IllegalStateException(
                        "eventDomainUser and eventDomainUserPassword properties must not be null");
            }
            EventDomainUser eventDomainUser = new EventDomainUser();
            eventDomainUser.setUsername(username);
            eventDomainUser.setHash(passwordEncoder.encode(password));
            eventDomainUserRepository.saveAndFlush(eventDomainUser);

            EventDomainUser eventDomainUser2 = new EventDomainUser();
            eventDomainUser2.setUsername(username + "admin");
            eventDomainUser2.setHash(passwordEncoder.encode(password));
            eventDomainUserRepository.saveAndFlush(eventDomainUser2);
        }
    }
}
