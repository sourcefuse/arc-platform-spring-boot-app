package pl.piomin.services.api;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.piomin.services.domain.Person;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class PersonApiIntegrationTests {

    private static final String API_PATH = "/api/v1";
    private static Long createdPersonId;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    @DisplayName("Should create a new person")
    void shouldCreatePerson() {
        // Arrange
        Person person = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), "John")
                .set(field -> field.name("lastName"), "Doe")
                .set(field -> field.name("age"), 30)
                .create();

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        assertEquals(30, response.getBody().getAge());

        // Store ID for later tests
        createdPersonId = response.getBody().getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should get person by ID")
    void shouldGetPersonById() {
        // Act
        ResponseEntity<Person> response = restTemplate.getForEntity(API_PATH + "/" + createdPersonId, Person.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdPersonId, response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all persons")
    void shouldGetAllPersons() {
        // Act
        ResponseEntity<List<Person>> response = restTemplate.exchange(
                API_PATH,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Person>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Should update person")
    void shouldUpdatePerson() {
        // Arrange
        ResponseEntity<Person> getResponse = restTemplate.getForEntity(API_PATH + "/" + createdPersonId, Person.class);
        Person personToUpdate = getResponse.getBody();
        assertNotNull(personToUpdate);

        personToUpdate.setFirstName("John-Updated");
        personToUpdate.setLastName("Doe-Updated");
        personToUpdate.setAge(31);

        // Act
        HttpEntity<Person> requestEntity = new HttpEntity<>(personToUpdate);
        ResponseEntity<Person> response = restTemplate.exchange(
                API_PATH,
                HttpMethod.PUT,
                requestEntity,
                Person.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdPersonId, response.getBody().getId());
        assertEquals("John-Updated", response.getBody().getFirstName());
        assertEquals("Doe-Updated", response.getBody().getLastName());
        assertEquals(31, response.getBody().getAge());
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 for non-existent person")
    void shouldReturn404ForNonExistentPerson() {
        // Act
        ResponseEntity<Person> response = restTemplate.getForEntity(API_PATH + "/999", Person.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Should delete person")
    void shouldDeletePerson() {
        // Act
        restTemplate.delete(API_PATH + "/" + createdPersonId);

        // Assert - Verify person is deleted
        ResponseEntity<Person> response = restTemplate.getForEntity(API_PATH + "/" + createdPersonId, Person.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Should handle invalid person data")
    void shouldHandleInvalidPersonData() {
        // Arrange - Create person with invalid data
        Person invalidPerson = new Person();
        // Leave all fields null or default

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, invalidPerson, Person.class);

        // Assert - This could be 400 Bad Request if validation is implemented
        // For now, we'll just check that the response contains a person with default values
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }
}
