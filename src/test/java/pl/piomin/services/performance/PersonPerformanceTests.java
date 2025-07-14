package pl.piomin.services.performance;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.piomin.services.domain.Person;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("performance")
public class PersonPerformanceTests {

    private static final String API_PATH = "/api/v1";
    private static final int CONCURRENT_REQUESTS = 10;
    private static final int TOTAL_REQUESTS = 50;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should handle concurrent GET requests efficiently")
    void shouldHandleConcurrentGetRequests() throws Exception {
        // Arrange - Create some test data first
        List<Long> personIds = createTestPersons(5);
        assertFalse(personIds.isEmpty(), "Failed to create test persons");

        // Act - Execute concurrent GET requests
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        Instant start = Instant.now();

        List<CompletableFuture<ResponseEntity<Person>>> futures = IntStream.range(0, TOTAL_REQUESTS)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    // Get a random person ID from our created list
                    Long personId = personIds.get(i % personIds.size());
                    return restTemplate.getForEntity(API_PATH + "/" + personId, Person.class);
                }, executor))
                .collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(); // Wait for completion

        Duration duration = Duration.between(start, Instant.now());

        // Assert - Check results and performance
        for (CompletableFuture<ResponseEntity<Person>> future : futures) {
            ResponseEntity<Person> response = future.get();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getId());
        }

        // Log performance metrics
        System.out.println("Completed " + TOTAL_REQUESTS + " requests in " + duration.toMillis() + "ms");
        System.out.println("Average response time: " + (duration.toMillis() / TOTAL_REQUESTS) + "ms per request");

        // Cleanup
        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle concurrent POST requests efficiently")
    void shouldHandleConcurrentPostRequests() throws Exception {
        // Arrange - Prepare test data
        List<Person> personsToCreate = IntStream.range(0, TOTAL_REQUESTS)
                .mapToObj(i -> Instancio.of(Person.class)
                        .ignore(field -> field.name("id"))
                        .set(field -> field.name("firstName"), "Concurrent-" + i)
                        .set(field -> field.name("lastName"), "Test-" + i)
                        .set(field -> field.name("age"), 20 + (i % 50))
                        .create())
                .collect(Collectors.toList());

        // Act - Execute concurrent POST requests
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        Instant start = Instant.now();

        List<CompletableFuture<ResponseEntity<Person>>> futures = personsToCreate.stream()
                .map(person -> CompletableFuture.supplyAsync(() ->
                        restTemplate.postForEntity(API_PATH, person, Person.class), executor))
                .collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(); // Wait for completion

        Duration duration = Duration.between(start, Instant.now());

        // Assert - Check results and performance
        for (CompletableFuture<ResponseEntity<Person>> future : futures) {
            ResponseEntity<Person> response = future.get();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getId());
        }

        // Log performance metrics
        System.out.println("Completed " + TOTAL_REQUESTS + " POST requests in " + duration.toMillis() + "ms");
        System.out.println("Average response time: " + (duration.toMillis() / TOTAL_REQUESTS) + "ms per request");

        // Cleanup
        executor.shutdown();
    }

    /**
     * Helper method to create test persons for performance testing
     *
     * @param count Number of persons to create
     * @return List of created person IDs
     */
    private List<Long> createTestPersons(int count) {
        List<Long> personIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Person person = Instancio.of(Person.class)
                    .ignore(field -> field.name("id"))
                    .set(field -> field.name("firstName"), "Performance-" + i)
                    .set(field -> field.name("lastName"), "Test-" + i)
                    .set(field -> field.name("age"), 25 + i)
                    .create();

            ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                personIds.add(response.getBody().getId());
            }
        }

        return personIds;
    }

    @Test
    @DisplayName("Should retrieve all persons efficiently")
    void shouldRetrieveAllPersonsEfficiently() {
        // Arrange - Create some test data first
        createTestPersons(20); // Create 20 test persons

        // Act - Measure time to retrieve all persons
        Instant start = Instant.now();
        ResponseEntity<List<Person>> response = restTemplate.exchange(
                API_PATH,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Person>>() {}
        );
        Duration duration = Duration.between(start, Instant.now());

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());

        // Log performance metrics
        System.out.println("Retrieved " + response.getBody().size() + " persons in " + duration.toMillis() + "ms");
    }
}
