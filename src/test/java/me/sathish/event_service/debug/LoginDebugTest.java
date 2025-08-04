package me.sathish.event_service.debug;

import me.sathish.event_service.config.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginDebugTest extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void debugLoginFlow() {
        System.out.println("=== Testing GET /login ===");
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity("/login", String.class);
        System.out.println("Login page status: " + loginPageResponse.getStatusCode());
        System.out.println("Login page body length: " + 
            (loginPageResponse.getBody() != null ? loginPageResponse.getBody().length() : "null"));
        
        if (loginPageResponse.getStatusCode() == HttpStatus.OK) {
            System.out.println(" Login page accessible");
        } else {
            System.out.println(" Login page not accessible: " + loginPageResponse.getStatusCode());
        }

        System.out.println("=== Testing API endpoint with authentication ===");
        ResponseEntity<String> apiResponse = restTemplate
                .withBasicAuth("sathish", "password")
                .getForEntity("/api/domains", String.class);
        System.out.println("API response status: " + apiResponse.getStatusCode());
        
        if (apiResponse.getStatusCode() == HttpStatus.OK) {
            System.out.println(" API endpoint accessible with basic auth");
        } else {
            System.out.println(" API endpoint not accessible: " + apiResponse.getStatusCode());
        }

        System.out.println("=== Testing API endpoint without authentication ===");
        ResponseEntity<String> unauthResponse = restTemplate.getForEntity("/api/domains", String.class);
        System.out.println("Unauth API response status: " + unauthResponse.getStatusCode());
        
        if (unauthResponse.getStatusCode() == HttpStatus.FORBIDDEN) {
            System.out.println(" API endpoint properly protected (403 Forbidden)");
        } else {
            System.out.println(" API endpoint security issue: " + unauthResponse.getStatusCode());
        }
    }
}
