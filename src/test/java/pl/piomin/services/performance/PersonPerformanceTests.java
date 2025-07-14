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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("performance")
public class PersonPerformanceTests {

    private static final String API_PATH = "/api/v1";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should handle concurrent create requests")
    void shouldHandleConcurrentCreateRequests() throws Exception {
        // Arrange
        int concurrentRequests = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<ResponseEntity<Person>>> futures = new ArrayList<>();
        
        // Act
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrentRequests; i++) {
            CompletableFuture<ResponseEntity<Person>> future = CompletableFuture.supplyAsync(() -> {
                Person person = Instancio.create(Person.class);
                person.setId(null);
                return restTemplate.postForEntity(API_PATH, person, Person.class);
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        allFutures.get(30, TimeUnit.SECONDS); // Wait with timeout
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Assert
        for (CompletableFuture<ResponseEntity<Person>> future : futures) {
            ResponseEntity<Person> response = future.get();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getId());
        }
        
        // Log performance metrics
        System.out.println("Total time for " + concurrentRequests + " concurrent requests: " + totalTime + "ms");
        System.out.println("Average time per request: " + (totalTime / concurrentRequests) + "ms");
        
        executorService.shutdown();
    }

    @Test
    @DisplayName("Should handle concurrent read requests")
    void shouldHandleConcurrentReadRequests() throws Exception {
        // Arrange - Create a person first
        Person person = Instancio.create(Person.class);
        person.setId(null);
        ResponseEntity<Person> createResponse = restTemplate.postForEntity(API_PATH, person, Person.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(createResponse.getBody());
        Long personId = createResponse.getBody().getId();
        
        int concurrentRequests = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<ResponseEntity<Person>>> futures = new ArrayList<>();
        
        // Act
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrentRequests; i++) {
            CompletableFuture<ResponseEntity<Person>> future = CompletableFuture.supplyAsync(() -> {
                return restTemplate.getForEntity(API_PATH + "/" + personId, Person.class);
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        allFutures.get(30, TimeUnit.SECONDS); // Wait with timeout
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Assert
        for (CompletableFuture<ResponseEntity<Person>> future : futures) {
            ResponseEntity<Person> response = future.get();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(personId, response.getBody().getId());
        }
        
        // Log performance metrics
        System.out.println("Total time for " + concurrentRequests + " concurrent read requests: " + totalTime + "ms");
        System.out.println("Average time per read request: " + (totalTime / concurrentRequests) + "ms");
        
        executorService.shutdown();
    }

    @Test
    @DisplayName("Should handle sequential load test")
    void shouldHandleSequentialLoadTest() {
        // Arrange
        int requestCount = 100;
        List<Long> createdIds = new ArrayList<>();
        
        // Act - Create
        long createStartTime = System.currentTimeMillis();
        
        for (int i = 0; i < requestCount; i++) {
            Person person = Instancio.create(Person.class);
            person.setId(null);
            ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            createdIds.add(response.getBody().getId());
        }
        
        long createEndTime = System.currentTimeMillis();
        long createTotalTime = createEndTime - createStartTime;
        
        // Act - Read
        long readStartTime = System.currentTimeMillis();
        
        for (Long id : createdIds) {
            ResponseEntity<Person> response = restTemplate.getForEntity(API_PATH + "/" + id, Person.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
        
        long readEndTime = System.currentTimeMillis();
        long readTotalTime = readEndTime - readStartTime;
        
        // Log performance metrics
        System.out.println("Total time for " + requestCount + " sequential create requests: " + createTotalTime + "ms");
        System.out.println("Average time per create request: " + (createTotalTime / requestCount) + "ms");
        System.out.println("Total time for " + requestCount + " sequential read requests: " + readTotalTime + "ms");
        System.out.println("Average time per read request: " + (readTotalTime / requestCount) + "ms");
    }
}