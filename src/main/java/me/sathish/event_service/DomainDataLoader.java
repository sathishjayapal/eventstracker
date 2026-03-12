package me.sathish.event_service;

import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@Order(2)
public class DomainDataLoader implements ApplicationRunner {
    private static final String RUNS_NAME_PROP = "eventTracker.domain.runs.name";
    private static final String RUNS_STATUS_PROP = "eventTracker.domain.runs.status";
    private static final String RUNS_COMMENTS_PROP = "eventTracker.domain.runs.comments";
    private static final String GITHUB_NAME_PROP = "eventTracker.domain.github.name";
    private static final String GITHUB_STATUS_PROP = "eventTracker.domain.github.status";
    private static final String GITHUB_COMMENTS_PROP = "eventTracker.domain.github.comments";

    private final DomainRepository domainRepository;
    private final EventDomainUserRepository eventDomainUserRepository;
    private final Environment environment;

    public DomainDataLoader(
            DomainRepository domainRepository,
            EventDomainUserRepository eventDomainUserRepository,
            Environment environment) {
        this.domainRepository = domainRepository;
        this.eventDomainUserRepository = eventDomainUserRepository;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        final String configuredUser = environment.getProperty("eventDomainUser");
        final String adminUser = environment.getProperty("eventTrackerAdminUser", configuredUser == null ? null : configuredUser + "admin");

        if (adminUser == null || adminUser.isBlank()) {
            throw new IllegalStateException("Domain seeding aborted: 'eventTrackerAdminUser' must be configured.");
        }

        if (!eventDomainUserRepository.existsByUsernameIgnoreCase(adminUser)) {
            throw new IllegalStateException("Domain seeding aborted: admin user '%s' not found.".formatted(adminUser));
        }

        final String runsName = requireNonBlank(environment.getProperty(RUNS_NAME_PROP, "RUNS_DOMAIN"), RUNS_NAME_PROP);
        final String runsStatus = environment.getProperty(RUNS_STATUS_PROP, "ACTIVE");
        final String runsComments = environment.getProperty(RUNS_COMMENTS_PROP, "Seed domain for RUNS integration tests.");

        final String githubName = requireNonBlank(environment.getProperty(GITHUB_NAME_PROP, "GITHUB_DOMAIN"), GITHUB_NAME_PROP);
        final String githubStatus = environment.getProperty(GITHUB_STATUS_PROP, "ACTIVE");
        final String githubComments = environment.getProperty(GITHUB_COMMENTS_PROP, "Seed domain for GitHub integration tests.");

        seedDomainIfMissing(runsName, runsStatus, runsComments);
        seedDomainIfMissing(githubName, githubStatus, githubComments);
    }

    private String requireNonBlank(String value, String propertyName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Domain seeding aborted: property '%s' must be set.".formatted(propertyName));
        }
        return value;
    }

    private void seedDomainIfMissing(String domainName, String status, String comments) {
        domainRepository
                .findByDomainNameIgnoreCase(domainName)
                .ifPresentOrElse(
                        existing -> log.debug("Domain {} already present with id {}", domainName, existing.getId()),
                        () -> {
                            Domain domain = new Domain();
                            domain.setDomainName(domainName);
                            domain.setStatus(status);
                            domain.setComments(comments);
                            domain.setDateCreated(OffsetDateTime.now());
                            domain.setLastUpdated(OffsetDateTime.now());
                            Domain saved = domainRepository.save(domain);
                            log.info("Seeded domain {} with id {} by admin validation", domainName, saved.getId());
                        });
    }
}
