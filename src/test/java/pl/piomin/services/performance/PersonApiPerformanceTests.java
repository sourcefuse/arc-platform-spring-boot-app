package pl.piomin.services.performance;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import pl.piomin.services.domain.Person;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("performance")
public class PersonApiPerformanceTests {

    private static final String API_PATH = "/api/v1";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should handle load of 100 sequential requests")
    void shouldHandleSequentialLoad() {
        // Arrange
        int requestCount = 100;
        List<Long> createdIds = new ArrayList<>();
        
        // Act - Measure time for creating 100 persons
        Instant start = Instant.now();
        
        for (int i = 0; i < requestCount; i++) {
            Person person = Instancio.of(Person.class)
                    .ignore(field -> field.name("id"))
                    .set(field -> field.name("firstName"), "LoadTest" + i)
                    .create();
            
            ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            createdIds.add(response.getBody().getId());
        }
        
        Duration duration = Duration.between(start, Instant.now());
        
        // Assert
        System.out.println("Sequential creation of " + requestCount + " persons took " + 
                duration.toMillis() + "ms (" + (duration.toMillis() / requestCount) + "ms per request)");
        
        assertTrue(duration.toMillis() < 30000, "Sequential creation should complete within 30 seconds");
        
        // Cleanup - Delete created persons
        for (Long id : createdIds) {
            restTemplate.delete(API_PATH + "/{id}", id);
        }
    }

    @Test
    @DisplayName("Should handle concurrent load of 50 parallel requests")
    void shouldHandleConcurrentLoad() throws Exception {
        // Arrange
        int concurrentRequests = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // Act - Measure time for creating persons concurrently
        Instant start = Instant.now();
        
        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                Person person = Instancio.of(Person.class)
                        .ignore(field -> field.name("id"))
                        .set(field -> field.name("firstName"), "ConcurrentTest" + index)
                        .create();
                
                ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
                assertEquals(200, response.getStatusCodeValue());
                assertNotNull(response.getBody());
                return response.getBody().getId();
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        allFutures.get(60, TimeUnit.SECONDS); // Wait up to 60 seconds
        
        Duration duration = Duration.between(start, Instant.now());
        
        // Get all created IDs for cleanup
        List<Long> createdIds = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            createdIds.add(future.get());
        }
        
        // Assert
        System.out.println("Concurrent creation of " + concurrentRequests + " persons took " + 
                duration.toMillis() + "ms (" + (duration.toMillis() / concurrentRequests) + "ms per request)");
        
        assertTrue(duration.toMillis() < 30000, "Concurrent creation should complete within 30 seconds");
        
        // Cleanup - Delete created persons
        for (Long id : createdIds) {
            restTemplate.delete(API_PATH + "/{id}", id);
        }
        
        executorService.shutdown();
    }

    @Test
    @DisplayName("Should handle stress test with rapid sequential requests")
    void shouldHandleStressTest() {
        // Arrange
        int requestCount = 20;
        List<Long> createdIds = new ArrayList<>();
        
        // Act - Create and immediately retrieve persons
        Instant start = Instant.now();
        
        for (int i = 0; i < requestCount; i++) {
            // Create person
            Person person = Instancio.of(Person.class)
                    .ignore(field -> field.name("id"))
                    .set(field -> field.name("firstName"), "StressTest" + i)
                    .create();
            
            ResponseEntity<Person> createResponse = restTemplate.postForEntity(API_PATH, person, Person.class);
            assertEquals(200, createResponse.getStatusCodeValue());
            assertNotNull(createResponse.getBody());
            Long id = createResponse.getBody().getId();
            createdIds.add(id);
            
            // Immediately retrieve it
            ResponseEntity<Person> getResponse = restTemplate.getForEntity(API_PATH + "/{id}", Person.class, id);
            assertEquals(200, getResponse.getStatusCodeValue());
            assertNotNull(getResponse.getBody());
            assertEquals(id, getResponse.getBody().getId());
            
            // And update it
            person.setId(id);
            person.setFirstName("Updated" + i);
            restTemplate.put(API_PATH + "/{id}", person, id);
            
            // And retrieve it again
            ResponseEntity<Person> updatedResponse = restTemplate.getForEntity(API_PATH + "/{id}", Person.class, id);
            assertEquals(200, updatedResponse.getStatusCodeValue());
            assertNotNull(updatedResponse.getBody());
            assertEquals("Updated" + i, updatedResponse.getBody().getFirstName());
        }
        
        Duration duration = Duration.between(start, Instant.now());
        
        // Assert
        System.out.println("Stress test with " + requestCount + " create-read-update-read cycles took " + 
                duration.toMillis() + "ms (" + (duration.toMillis() / requestCount) + "ms per cycle)");
        
        assertTrue(duration.toMillis() < 30000, "Stress test should complete within 30 seconds");
        
        // Cleanup - Delete created persons
        for (Long id : createdIds) {
            restTemplate.delete(API_PATH + "/{id}", id);
        }
    }

    @Test
    @DisplayName("Should handle repeated reads efficiently")
    void shouldHandleRepeatedReadsEfficiently() {
        // Arrange - Create a person first
        Person person = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .create();
        
        ResponseEntity<Person> createResponse = restTemplate.postForEntity(API_PATH, person, Person.class);
        assertEquals(200, createResponse.getStatusCodeValue());
        assertNotNull(createResponse.getBody());
        Long personId = createResponse.getBody().getId();
        
        int readCount = 100;
        
        // Act - Measure time for repeated reads
        Instant start = Instant.now();
        
        for (int i = 0; i < readCount; i++) {
            ResponseEntity<Person> response = restTemplate.getForEntity(API_PATH + "/{id}", Person.class, personId);
            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            assertEquals(personId, response.getBody().getId());
        }
        
        Duration duration = Duration.between(start, Instant.now());
        
        // Assert
        System.out.println("Repeated reading of a person " + readCount + " times took " + 
                duration.toMillis() + "ms (" + (duration.toMillis() / readCount) + "ms per read)");
        
        assertTrue(duration.toMillis() < 10000, "Repeated reads should complete within 10 seconds");
        
        // Cleanup
        restTemplate.delete(API_PATH + "/{id}", personId);
    }
}
