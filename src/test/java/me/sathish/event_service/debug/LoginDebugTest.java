package me.sathish.event_service.debug;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.sathish.event_service.config.BaseIT;
import org.junit.jupiter.api.Test;

public class LoginDebugTest extends BaseIT {

    @Test
    void debugLoginFlow() {
        System.out.println("=== Testing GET /login ===");
        String initialSession = RestAssured.given()
                .accept(ContentType.HTML)
                .when()
                .get("/login")
                .then()
                .statusCode(200)
                .extract()
                .sessionId();

        System.out.println("Initial session ID: " + initialSession);

        System.out.println("=== Testing POST /login ===");
        try {
            String loginSession = RestAssured.given()
                    .sessionId(initialSession)
                    .csrf("/login")
                    .accept(ContentType.HTML)
                    .contentType(ContentType.URLENC)
                    .formParam("username", AUTH_USER)
                    .formParam("password", PASSWORD)
                    .when()
                    .post("/login")
                    .then()
                    .log()
                    .all()
                    .extract()
                    .sessionId();

            System.out.println("Login session ID: " + loginSession);
        } catch (Exception e) {
            System.out.println("Login failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
