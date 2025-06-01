package me.sathish.event_service.config;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import me.sathish.event_service.EventServiceApplication;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.domain_event.DomainEventRepository;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.PostgreSQLContainer;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@SpringBootTest(
        classes = EventServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql({"/data/clearAll.sql", "/data/eventDomainUserData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.5");
    public static final String AUTH_USER = "authUser";
    public static final String PASSWORD = "Bootify!";
    private static String eventserviceconfigSession = null;

    static {
        postgreSQLContainer.withReuse(true)
                .start();
    }

    @LocalServerPort
    public int serverPort;

    @Autowired
    public DomainRepository domainRepository;

    @Autowired
    public DomainEventRepository domainEventRepository;

    @Autowired
    public EventDomainUserRepository eventDomainUserRepository;

    @PostConstruct
    public void initRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.urlEncodingEnabled = false;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @SneakyThrows
    public String readResource(final String resourceName) {
        return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    public String eventserviceconfigSession() {
        if (eventserviceconfigSession == null) {
            // init session
            eventserviceconfigSession = RestAssured
                    .given()
                        .accept(ContentType.HTML)
                    .when()
                        .get("/login")
                    .sessionId();

            // perform login
            eventserviceconfigSession = RestAssured
                    .given()
                        .sessionId(eventserviceconfigSession)
                        .csrf("/login")
                        .accept(ContentType.HTML)
                        .contentType(ContentType.URLENC)
                        .formParam("username", AUTH_USER)
                        .formParam("password", PASSWORD)
                    .when()
                        .post("/login")
                    .sessionId();
        }
        return eventserviceconfigSession;
    }

}
