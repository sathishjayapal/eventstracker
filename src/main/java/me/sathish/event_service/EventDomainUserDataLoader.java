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
        if (eventDomainUserRepository.findByUsernameIgnoreCase(environment.getProperty("eventDomainUser")) != null) {
            System.out.println("Event Domain User already exists");
        } else {
            EventDomainUser eventDomainUser = new EventDomainUser();
            eventDomainUser.setUsername(environment.getProperty("eventDomainUser"));
            eventDomainUser.setHash(passwordEncoder.encode(environment.getProperty("eventDomainUserPassword")));
            eventDomainUserRepository.saveAndFlush(eventDomainUser);

            EventDomainUser eventDomainUser2 = new EventDomainUser();
            eventDomainUser2.setUsername(environment.getProperty("eventDomainUser") + "admin");
            eventDomainUser2.setHash(passwordEncoder.encode(environment.getProperty("eventDomainUserPassword")));
            eventDomainUserRepository.saveAndFlush(eventDomainUser2);
        }
    }
}
