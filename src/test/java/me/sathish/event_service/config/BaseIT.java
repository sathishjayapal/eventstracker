package me.sathish.event_service.config;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.domain_event.DomainEventRepository;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainersConfig.class)
@Sql({"/data/clearAll.sql", "/data/eventDomainUserData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Slf4j
public abstract class BaseIT {

    public static final String AUTH_USER = "sathish";
    public static final String PASSWORD = "password";
    private static String eventserviceconfigSession = null;

    @LocalServerPort
    public int port;

    @Autowired
    public DomainRepository domainRepository;

    @Autowired
    public DomainEventRepository domainEventRepository;

    @Autowired
    public EventDomainUserRepository eventDomainUserRepository;

    @BeforeEach
    public void setupPort() {
        RestAssured.port = port;
    }

    @SneakyThrows
    public String readResource(final String resourceName) {
        return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    public String eventserviceconfigSession() {
        if (eventserviceconfigSession == null) {
            // init session
            eventserviceconfigSession = RestAssured.given()
                    .accept(ContentType.HTML)
                    .port(port)
                    .when()
                    .get("/login")
                    .sessionId();

            // perform login
            eventserviceconfigSession = RestAssured.given()
                    .sessionId(eventserviceconfigSession)
                    .csrf("/login")
                    .accept(ContentType.HTML)
                    .contentType(ContentType.URLENC)
                    .formParam("username", AUTH_USER)
                    .formParam("password", PASSWORD)
                    .port(port)
                    .when()
                    .post("/login")
                    .sessionId();
        }
        return eventserviceconfigSession;
    }
}
