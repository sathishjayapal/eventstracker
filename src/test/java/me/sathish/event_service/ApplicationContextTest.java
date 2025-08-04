package me.sathish.event_service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import me.sathish.event_service.config.BaseIT;
import me.sathish.event_service.domain.DomainResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Test to verify that the application context loads properly and controllers are registered.
 */
public class ApplicationContextTest extends BaseIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
        System.out.println("Application context loaded successfully");

        // Check if DomainResource bean is available
        DomainResource domainResource = applicationContext.getBean(DomainResource.class);
        assertNotNull(domainResource);
        System.out.println(
                "DomainResource bean found: " + domainResource.getClass().getName());

        // Print all beans to debug
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Total beans: " + beanNames.length);
        for (String beanName : beanNames) {
            if (beanName.contains("Resource") || beanName.contains("Controller")) {
                System.out.println("Controller/Resource bean: " + beanName);
            }
        }
    }
}
