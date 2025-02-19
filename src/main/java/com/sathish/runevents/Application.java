package com.sathish.runevents;

import com.sathish.runevents.domain.util.DomainDataServiceUtil;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableJpaRepositories(
        value = {"com.sathish.runevents.domain.repo", "com.sathish.runevents.events.repo"},
        repositoryBaseClass = BaseJpaRepositoryImpl.class)
public class Application implements CommandLineRunner {
    final DomainDataServiceUtil domainDataServiceUtil;

    public Application(DomainDataServiceUtil domainDataServiceUtil) {
        this.domainDataServiceUtil = domainDataServiceUtil;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        domainDataServiceUtil.loadDomainData();
    }
}

@Controller
class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
