package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.sathish.event_service.config.BaseIT;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(properties = {
    "eventDomainUser=sathish",
    "eventDomainUserPassword=password"
})
public class DomainEventResourceTest extends BaseIT {

    @Autowired
    private Environment environment;

    @Test
    @Sql("/data/domainEventData.sql")
    void getAllDomainEvents_success() {
        System.out.println("Domain user is" +environment.getProperty("eventDomainUser"));
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .when()
                .get("/api/domainEvents")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.equalTo(2))
                .body("get(0).id", Matchers.equalTo(1100));
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void getDomainEvent_success() {
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .when()
                .get("/api/domainEvents/1100")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(
                        "eventId",
                        Matchers.equalTo(
                                "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat."));
    }

    @Test
    void getDomainEvent_notFound() {
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .when()
                .get("/api/domainEvents/1766")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("code", Matchers.equalTo("NOT_FOUND"));
    }

    @Test
    void createDomainEvent_success() {
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(readResource("/requests/domainEventDTORequest.json"))
                .when()
                .post("/api/domainEvents")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        assertEquals(1, domainEventRepository.count());
    }

    @Test
    void createDomainEvent_missingField() {
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(readResource("/requests/domainEventDTORequest_missingField.json"))
                .when()
                .post("/api/domainEvents")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", Matchers.equalTo("VALIDATION_FAILED"))
                .body("fieldErrors.get(0).property", Matchers.equalTo("eventId"))
                .body("fieldErrors.get(0).code", Matchers.equalTo("REQUIRED_NOT_NULL"));
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void updateDomainEvent_success() {
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(readResource("/requests/domainEventDTORequest.json"))
                .when()
                .put("/api/domainEvents/1100")
                .then()
                .statusCode(HttpStatus.OK.value());
        assertEquals(
                "Vel eros donec ac odio tempor orci.",
                domainEventRepository.findById(((long) 1100)).orElseThrow().getEventId());
        assertEquals(2, domainEventRepository.count());
    }

    @Test
    @Sql("/data/domainEventData.sql")
    void deleteDomainEvent_success() {
        RestAssured.given()
                .sessionId(eventserviceconfigSession())
                .accept(ContentType.JSON)
                .when()
                .delete("/api/domainEvents/1100")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        assertEquals(1, domainEventRepository.count());
    }
}
