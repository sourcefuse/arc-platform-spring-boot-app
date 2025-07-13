package pl.piomin.services.integration;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerIntegrationTests {

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
                .create();

        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(API_PATH, person, Person.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        
        // Store ID for subsequent tests
        createdPersonId = response.getBody().getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve all persons")
    void shouldFindAllPersons() {
        // Act
        ResponseEntity<Person[]> response = restTemplate.getForEntity(API_PATH, Person[].class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(3)
    @DisplayName("Should retrieve person by ID")
    void shouldFindPersonById() {
        // Act
        ResponseEntity<Person> response = restTemplate.getForEntity(
                API_PATH + "/{id}", Person.class, createdPersonId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdPersonId, response.getBody().getId());
    }

    @Test
    @Order(4)
    @DisplayName("Should update existing person")
    void shouldUpdatePerson() {
        // Arrange
        Person personToUpdate = restTemplate.getForObject(
                API_PATH + "/{id}", Person.class, createdPersonId);
        assertNotNull(personToUpdate);
        
        String updatedFirstName = "UpdatedFirstName";
        String updatedLastName = "UpdatedLastName";
        personToUpdate.setFirstName(updatedFirstName);
        personToUpdate.setLastName(updatedLastName);

        // Act
        ResponseEntity<Person> response = restTemplate.exchange(
                API_PATH + "/{id}", 
                HttpMethod.PUT, 
                new HttpEntity<>(personToUpdate), 
                Person.class, 
                createdPersonId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedFirstName, response.getBody().getFirstName());
        assertEquals(updatedLastName, response.getBody().getLastName());
    }

    @Test
    @Order(5)
    @DisplayName("Should delete person")
    void shouldDeletePerson() {
        // Act
        restTemplate.delete(API_PATH + "/{id}", createdPersonId);
        
        // Assert - Verify the person no longer exists
        ResponseEntity<Person> response = restTemplate.getForEntity(
                API_PATH + "/{id}", Person.class, createdPersonId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 when person not found")
    void shouldReturn404WhenPersonNotFound() {
        // Act
        ResponseEntity<Person> response = restTemplate.getForEntity(
                API_PATH + "/{id}", Person.class, 9999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
