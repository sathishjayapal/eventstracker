package me.sathish.event_service.config;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.domain_event.DomainEventRepository;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"server.port=0"})
@Sql({"/data/clearAll.sql", "/data/eventDomainUserData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.5");

    public static final String AUTH_USER = "sathish";
    public static final String PASSWORD = "password";
    private static String eventserviceconfigSession = null;

    static {
        postgreSQLContainer.withReuse(true).start();
    }

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    @Autowired
    public DomainRepository domainRepository;

    @Autowired
    public DomainEventRepository domainEventRepository;

    @Autowired
    public EventDomainUserRepository eventDomainUserRepository;

    private volatile int actualPort = 0;

    @BeforeEach
    public void setupPort() throws InterruptedException {
        // Give the server a moment to fully start
        Thread.sleep(100);

        System.out.println("=== DEBUG: setupPort() called ===");
        System.out.println("@LocalServerPort field: " + port);
        System.out.println("local.server.port: " + environment.getProperty("local.server.port"));
        System.out.println("server.port: " + environment.getProperty("server.port"));

        // For fixed port testing, use the configured port
        String serverPort = environment.getProperty("server.port");
        if (serverPort != null && !serverPort.equals("0")) {
            try {
                actualPort = Integer.parseInt(serverPort);
                System.out.println("Using server.port: " + actualPort);
                return;
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse server.port: " + serverPort);
            }
        }

        // Try @LocalServerPort first
        if (port > 0) {
            actualPort = port;
            System.out.println("Using @LocalServerPort: " + actualPort);
            return;
        }

        // Try environment properties
        String localServerPort = environment.getProperty("local.server.port");
        if (localServerPort != null && !localServerPort.equals("0")) {
            try {
                actualPort = Integer.parseInt(localServerPort);
                System.out.println("Using local.server.port: " + actualPort);
                return;
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse local.server.port: " + localServerPort);
            }
        }

        // Last resort: scan for the actual port the server is listening on
        System.out.println("Scanning for actual server port...");
        actualPort = findServerPort();
        if (actualPort > 0) {
            System.out.println("Found server listening on port: " + actualPort);
            return;
        }

        System.out.println("ERROR: Could not determine server port in setupPort()");
        throw new IllegalStateException("Cannot determine server port - all sources returned 0 or invalid values");
    }

    private int findServerPort() {
        // Scan common Spring Boot test port ranges
        for (int testPort = 8080; testPort <= 8090; testPort++) {
            if (isPortListening(testPort)) {
                return testPort;
            }
        }

        // Scan higher random port ranges
        for (int testPort = 49152; testPort <= 49200; testPort++) {
            if (isPortListening(testPort)) {
                return testPort;
            }
        }

        return 0;
    }

    private boolean isPortListening(int testPort) {
        try (Socket socket = new Socket("localhost", testPort)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void init() {
        System.out.println("=== DEBUG: init() called ===");
        System.out.println(
                "ApplicationContext type: " + applicationContext.getClass().getName());
        System.out.println("Is ServletWebServerApplicationContext? "
                + (applicationContext instanceof ServletWebServerApplicationContext));

        if (applicationContext instanceof ServletWebServerApplicationContext) {
            ServletWebServerApplicationContext servletWebServerApplicationContext =
                    (ServletWebServerApplicationContext) applicationContext;
            actualPort = servletWebServerApplicationContext.getWebServer().getPort();
            System.out.println("=== DEBUG: ServletWebServerApplicationContext used ===");
            System.out.println("Port from ApplicationContext: " + actualPort);
        } else {
            System.out.println("ApplicationContext is not ServletWebServerApplicationContext, cannot get port");
        }
    }

    protected int getActualPort() {
        System.out.println("=== DEBUG: getActualPort() called ===");
        System.out.println("Returning actualPort: " + actualPort);
        return actualPort;
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
                    .port(getActualPort())
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
                    .port(getActualPort())
                    .when()
                    .post("/login")
                    .sessionId();
        }
        return eventserviceconfigSession;
    }
}
