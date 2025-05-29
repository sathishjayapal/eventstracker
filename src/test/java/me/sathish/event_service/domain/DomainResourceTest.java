package me.sathish.event_service.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.sathish.event_service.config.BaseIT;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;


public class DomainResourceTest extends BaseIT {

    @Test
    @Sql("/data/domainData.sql")
    void getAllDomains_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/domains")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", Matchers.equalTo(2))
                    .body("get(0).id", Matchers.equalTo(1000));
    }

    @Test
    @Sql("/data/domainData.sql")
    void getDomain_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/domains/1000")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("domainName", Matchers.equalTo("Donec pretium vulputate sapien nec sagittis aliquam malesuada."));
    }

    @Test
    void getDomain_notFound() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/domains/1666")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", Matchers.equalTo("NOT_FOUND"));
    }

    @Test
    void createDomain_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/domainDTORequest.json"))
                .when()
                    .post("/api/domains")
                .then()
                    .statusCode(HttpStatus.CREATED.value());
        assertEquals(1, domainRepository.count());
    }

    @Test
    void createDomain_missingField() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/domainDTORequest_missingField.json"))
                .when()
                    .post("/api/domains")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", Matchers.equalTo("VALIDATION_FAILED"))
                    .body("fieldErrors.get(0).property", Matchers.equalTo("domainName"))
                    .body("fieldErrors.get(0).code", Matchers.equalTo("REQUIRED_NOT_NULL"));
    }

    @Test
    @Sql("/data/domainData.sql")
    void updateDomain_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/domainDTORequest.json"))
                .when()
                    .put("/api/domains/1000")
                .then()
                    .statusCode(HttpStatus.OK.value());
        assertEquals("Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam.", domainRepository.findById(((long)1000)).orElseThrow().getDomainName());
        assertEquals(2, domainRepository.count());
    }

    @Test
    @Sql("/data/domainData.sql")
    void deleteDomain_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .delete("/api/domains/1000")
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        assertEquals(1, domainRepository.count());
    }

}
