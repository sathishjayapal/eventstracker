package me.sathish.event_service.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import me.sathish.event_service.config.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eventDomainUser=sathish", "eventDomainUserPassword=password"})
public class DomainResourceTest extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DomainRepository domainRepository;

    @Test
    @Sql("/data/domainData.sql")
    void getAllDomains_success() {
        ResponseEntity<String> response =
                restTemplate.withBasicAuth("sathish", "password").getForEntity("/api/domains", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verify we get the expected domain data
        String body = response.getBody();
        assert body.contains("1000");
        assert body.contains("1001");
    }

    @Test
    void getAllDomains_unauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/domains", String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Sql("/data/domainData.sql")
    void getDomain_success() {
        ResponseEntity<String> response =
                restTemplate.withBasicAuth("sathish", "password").getForEntity("/api/domains/1000", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assert response.getBody().contains("1000");
    }

    @Test
    void getDomain_notFound() {
        ResponseEntity<String> response =
                restTemplate.withBasicAuth("sathish", "password").getForEntity("/api/domains/999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createDomain_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(readResource("/requests/domainDTORequest.json"), headers);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sathish", "password")
                .postForEntity("/api/domains", requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, domainRepository.count());
    }

    @Test
    void createDomain_missingField() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity =
                new HttpEntity<>(readResource("/requests/domainDTORequest_missingField.json"), headers);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sathish", "password")
                .postForEntity("/api/domains", requestEntity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql("/data/domainData.sql")
    void updateDomain_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(readResource("/requests/domainDTORequest.json"), headers);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sathish", "password")
                .exchange("/api/domains/1000", HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam.",
                domainRepository.findById(1000L).orElseThrow().getDomainName());
    }

    @Test
    @Sql("/data/domainData.sql")
    void deleteDomain_success() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sathish", "password")
                .exchange("/api/domains/1000", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(1, domainRepository.count());
    }
}
