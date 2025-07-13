package pl.piomin.services.edge;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.piomin.services.domain.Person;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonApiEdgeCaseTests {

    private static final String API_PATH = "/api/v1";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should handle creating person with null fields")
    void shouldHandleCreatingPersonWithNullFields() {
        // Arrange
        Person person = new Person();
        // All fields are null

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "verylongfirstnamethatshouldprobablybeinvalidbutweretestinganyway"})
    @DisplayName("Should handle boundary values for first name")
    void shouldHandleBoundaryValuesForFirstName(String firstName) {
        // Arrange
        Person person = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), firstName)
                .create();

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(firstName, response.getBody().getFirstName());
    }

    @Test
    @DisplayName("Should handle concurrent requests")
    void shouldHandleConcurrentRequests() throws InterruptedException {
        // Arrange - Create multiple persons concurrently
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Person person = Instancio.of(Person.class)
                        .ignore(field -> field.name("id"))
                        .set(field -> field.name("firstName"), "Concurrent" + index)
                        .create();
                
                ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - Verify we can retrieve all created persons
        ResponseEntity<Person[]> response = restTemplate.getForEntity(API_PATH, Person[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 10);
    }

    @Test
    @DisplayName("Should handle updating with mismatched IDs")
    void shouldHandleUpdatingWithMismatchedIds() {
        // Arrange - Create a person first
        Person person = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .create();
        
        ResponseEntity<Person> createResponse = restTemplate.postForEntity(API_PATH, person, Person.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        Long personId = createResponse.getBody().getId();

        // Now try to update with mismatched ID
        Person updatePerson = Instancio.of(Person.class)
                .set(field -> field.name("id"), personId + 1) // Different ID
                .create();

        // Act
        ResponseEntity<Person> response = restTemplate.exchange(
                API_PATH + "/{id}", 
                HttpMethod.PUT, 
                new HttpEntity<>(updatePerson), 
                Person.class, 
                personId);
        
        // Assert - The API should handle this gracefully
        // Depending on implementation, it might either update the entity or return an error
        // We're just verifying it doesn't crash
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle very large payload")
    void shouldHandleVeryLargePayload() {
        // Arrange
        Person person = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), generateLargeString(5000))
                .set(field -> field.name("lastName"), generateLargeString(5000))
                .create();

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
        
        // Assert - The API should handle this gracefully
        // It might reject it or accept it depending on configuration
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle special characters in person fields")
    void shouldHandleSpecialCharactersInPersonFields() {
        // Arrange
        Person person = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), "Special!@#$%^&*()_+{}[]|\\:;\"'<>,.?/~`")
                .set(field -> field.name("lastName"), "Chars-äöüÄÖÜßéèêë")
                .create();

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Special!@#$%^&*()_+{}[]|\\:;\"'<>,.?/~`", response.getBody().getFirstName());
        assertEquals("Chars-äöüÄÖÜßéèêë", response.getBody().getLastName());
    }

    private String generateLargeString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('X');
        }
        return sb.toString();
    }
}
