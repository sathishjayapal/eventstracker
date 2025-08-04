package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.sathish.event_service.config.BaseIT;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DomainEventResourceTest extends BaseIT {

    @Autowired
    private Environment environment;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Sql("/data/domainEventData.sql")
    void getAllDomainEvents_success() {
        System.out.println("Domain user is" + environment.getProperty("eventDomainUser"));
        
        // GET request to retrieve all domain events
        ResponseEntity<Object[]> response = restTemplate
                .withBasicAuth("sathish", "password")
                .getForEntity("http://localhost:" + getActualPort() + "/api/domainEvents", Object[].class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Domain events retrieved successfully");
        } else {
            System.out.println("Failed to retrieve domain events: " + response.getStatusCode());
        }
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void getDomainEvent_success() {
        // GET request for a specific domain event
        ResponseEntity<Object> response = restTemplate
                .withBasicAuth("sathish", "password")
                .getForEntity("http://localhost:" + getActualPort() + "/api/domainEvents/1100", Object.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("Domain event retrieved successfully");
    }

    @Test
    void getDomainEvent_notFound() {
        // GET request for a non-existent domain event
        ResponseEntity<Object> response = restTemplate
                .withBasicAuth("sathish", "password")
                .getForEntity("http://localhost:" + getActualPort() + "/api/domainEvents/1766", Object.class);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        System.out.println("Domain event not found");
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void createDomainEvent_success() {
        // POST request to create a new domain event
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = readResource("/requests/domainEventDTORequest.json");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Object> response = restTemplate
                .withBasicAuth("sathish", "password")
                .postForEntity("http://localhost:" + getActualPort() + "/api/domainEvents", request, Object.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        System.out.println("Domain event created successfully");
    }

    @Test
    void createDomainEvent_missingField() {
        // POST request to create a new domain event with missing field
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = readResource("/requests/domainEventDTORequest_missingField.json");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Object> response = restTemplate
                .withBasicAuth("sathish", "password")
                .postForEntity("http://localhost:" + getActualPort() + "/api/domainEvents", request, Object.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("Domain event creation failed due to missing field");
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void updateDomainEvent_success() {
        // PUT request to update an existing domain event
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = readResource("/requests/domainEventDTORequest.json");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Object> response = restTemplate
                .withBasicAuth("sathish", "password")
                .exchange("http://localhost:" + getActualPort() + "/api/domainEvents/1100", HttpMethod.PUT, request, Object.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("Domain event updated successfully");
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void deleteDomainEvent_success() {
        // DELETE request to delete an existing domain event
        ResponseEntity<Object> response = restTemplate
                .withBasicAuth("sathish", "password")
                .exchange("http://localhost:" + getActualPort() + "/api/domainEvents/1100", HttpMethod.DELETE, HttpEntity.EMPTY, Object.class);
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        System.out.println("Domain event deleted successfully");
    }
}
