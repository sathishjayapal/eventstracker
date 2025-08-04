package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import me.sathish.event_service.config.BaseIT;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

class DomainEventPerformanceIT extends BaseIT {

    @Autowired
    private DomainEventService domainEventService;

    private Domain testDomain;

    @BeforeEach
    void setUp() {
        testDomain = TestDataBuilder.domain()
                .withDomainName("Performance Test Domain")
                .build();
        testDomain = domainRepository.save(testDomain);
    }

    @Test
    @Timeout(30) // 30 seconds timeout
    void bulkCreate_ShouldHandleLargeNumberOfEvents() {
        // Given
        int numberOfEvents = 100;
        List<DomainEventDTO> events = TestDataBuilder.createMultipleDomainEventDTOs(numberOfEvents, testDomain.getId());

        // When
        Instant start = Instant.now();

        for (DomainEventDTO event : events) {
            domainEventService.create(event);
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Then
        List<DomainEventDTO> allEvents = domainEventService.findAll();
        assertEquals(numberOfEvents, allEvents.size());

        // Performance assertion - should complete within reasonable time
        assertTrue(duration.toSeconds() < 25, "Bulk creation took too long: " + duration.toSeconds() + " seconds");

        System.out.println("Created "
                + numberOfEvents
                + " events in "
                + duration.toMillis()
                + "ms (avg: "
                + (duration.toMillis() / numberOfEvents)
                + "ms per event)");
    }

    @Test
    @Timeout(15)
    void findAll_WithLargeDataset_ShouldPerformWell() {
        // Given - Create a substantial dataset
        int numberOfEvents = 50;
        List<DomainEventDTO> events = TestDataBuilder.createMultipleDomainEventDTOs(numberOfEvents, testDomain.getId());

        for (DomainEventDTO event : events) {
            domainEventService.create(event);
        }

        // When
        Instant start = Instant.now();
        List<DomainEventDTO> result = domainEventService.findAll();
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Then
        assertEquals(numberOfEvents, result.size());
        assertTrue(duration.toMillis() < 5000, "FindAll took too long: " + duration.toMillis() + "ms");

        System.out.println("Retrieved " + numberOfEvents + " events in " + duration.toMillis() + "ms");
    }

    @Test
    @Timeout(20)
    void concurrentCreate_ShouldHandleMultipleThreads() throws Exception {
        // Given
        int numberOfThreads = 5;
        int eventsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // When
        Instant start = Instant.now();

        CompletableFuture<?>[] futures = IntStream.range(0, numberOfThreads)
                .mapToObj(threadId -> CompletableFuture.runAsync(
                        () -> {
                            for (int i = 0; i < eventsPerThread; i++) {
                                DomainEventDTO event = TestDataBuilder.domainEventDTO()
                                        .withEventType("CONCURRENT_TEST_T" + threadId + "_E" + i)
                                        .withEventData("Concurrent test data from thread " + threadId + ", event " + i)
                                        .withDomain(testDomain.getId())
                                        .build();
                                domainEventService.create(event);
                            }
                        },
                        executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Then
        List<DomainEventDTO> allEvents = domainEventService.findAll();
        assertEquals(numberOfThreads * eventsPerThread, allEvents.size());

        assertTrue(
                duration.toSeconds() < 15, "Concurrent creation took too long: " + duration.toSeconds() + " seconds");

        System.out.println("Created " + (numberOfThreads * eventsPerThread) + " events concurrently in "
                + duration.toMillis() + "ms");

        executor.shutdown();
    }

    @Test
    @Timeout(10)
    void mixedOperations_ShouldMaintainPerformance() {
        // Given
        int numberOfOperations = 30;

        // When
        Instant start = Instant.now();

        for (int i = 0; i < numberOfOperations; i++) {
            // Create
            DomainEventDTO event = TestDataBuilder.domainEventDTO()
                    .withEventType("MIXED_OPS_EVENT_" + i)
                    .withEventData("Mixed operations test data " + i)
                    .withDomain(testDomain.getId())
                    .build();
            Long eventId = domainEventService.create(event);

            // Read
            DomainEventDTO retrieved = domainEventService.get(eventId);
            assertNotNull(retrieved);

            // Update (every 3rd event)
            if (i % 3 == 0) {
                DomainEventDTO updateDTO = TestDataBuilder.domainEventDTO()
                        .withEventType("UPDATED_MIXED_OPS_" + i)
                        .withEventData("Updated mixed operations data " + i)
                        .withDomain(testDomain.getId())
                        .build();
                domainEventService.update(eventId, updateDTO);
            }

            // Delete (every 5th event)
            if (i % 5 == 0) {
                domainEventService.delete(eventId);
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Then
        assertTrue(duration.toSeconds() < 8, "Mixed operations took too long: " + duration.toSeconds() + " seconds");

        System.out.println("Completed " + numberOfOperations + " mixed operations in " + duration.toMillis() + "ms");
    }

    @Test
    @Timeout(10)
    void memoryUsage_ShouldNotExceedReasonableLimits() throws InterruptedException {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        int numberOfEvents = 100;

        // When
        for (int i = 0; i < numberOfEvents; i++) {
            DomainEventDTO event = TestDataBuilder.domainEventDTO()
                    .withEventType("MEMORY_TEST_EVENT_" + i)
                    .withEventData("Memory test data " + i + " with some additional content to increase size")
                    .withDomain(testDomain.getId())
                    .build();
            domainEventService.create(event);
        }

        // Force garbage collection
        System.gc();
        Thread.sleep(100); // Give GC time to run

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Then
        System.out.println("Memory increase: " + (memoryIncrease / 1024 / 1024) + " MB");

        // Memory increase should be reasonable (less than 50MB for 100 events)
        assertTrue(
                memoryIncrease < 50 * 1024 * 1024,
                "Memory usage increased too much: " + (memoryIncrease / 1024 / 1024) + " MB");
    }

    @Test
    @Timeout(5)
    void databaseConnection_ShouldHandleRepeatedAccess() {
        // Given
        int numberOfAccesses = 50;

        // When
        Instant start = Instant.now();

        for (int i = 0; i < numberOfAccesses; i++) {
            // Alternate between different operations to test connection handling
            if (i % 2 == 0) {
                domainEventService.findAll();
            } else {
                DomainEventDTO event = TestDataBuilder.domainEventDTO()
                        .withEventType("DB_CONNECTION_TEST_" + i)
                        .withDomain(testDomain.getId())
                        .build();
                Long eventId = domainEventService.create(event);
                domainEventService.get(eventId);
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Then
        assertTrue(duration.toSeconds() < 3, "Database operations took too long: " + duration.toSeconds() + " seconds");

        System.out.println("Completed " + numberOfAccesses + " database operations in " + duration.toMillis() + "ms");
    }
}
