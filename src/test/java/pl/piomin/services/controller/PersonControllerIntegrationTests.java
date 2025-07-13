package pl.piomin.services.controller;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonControllerIntegrationTests {

    private static final String API_PATH = "/api/v1/persons";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PersonRepository personRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port + API_PATH;
    }

    private Person testPerson;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        personRepository.deleteAll();
        
        // Create test person
        testPerson = Instancio.create(Person.class);
        testPerson.setId(null); // Let the database assign ID
        
        // Set up headers
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should create a new person")
    void createPerson() {
        // Arrange
        HttpEntity<Person> request = new HttpEntity<>(testPerson, headers);
        
        // Act
        ResponseEntity<Person> response = restTemplate.postForEntity(
                getBaseUrl(), request, Person.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(testPerson.getFirstName(), response.getBody().getFirstName());
        assertEquals(testPerson.getLastName(), response.getBody().getLastName());
    }

    @Test
    @DisplayName("Should return all persons")
    void getAllPersons() {
        // Arrange - Create multiple persons
        Person person1 = createTestPerson("John", "Doe");
        Person person2 = createTestPerson("Jane", "Smith");
        
        // Act
        ResponseEntity<Person[]> response = restTemplate.getForEntity(
                getBaseUrl(), Person[].class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
        
        List<Person> persons = Arrays.asList(response.getBody());
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals("John")));
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals("Jane")));
    }

    @Test
    @DisplayName("Should return person by ID when exists")
    void getPersonById_Exists() {
        // Arrange
        Person savedPerson = createTestPerson("Test", "Person");
        
        // Act
        ResponseEntity<Person> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Person.class, savedPerson.getId());
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(savedPerson.getId(), response.getBody().getId());
        assertEquals("Test", response.getBody().getFirstName());
        assertEquals("Person", response.getBody().getLastName());
    }

    @Test
    @DisplayName("Should return not found when person doesn't exist")
    void getPersonById_NotExists() {
        // Act
        ResponseEntity<Person> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Person.class, 999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should update person when exists")
    void updatePerson_Exists() {
        // Arrange
        Person savedPerson = createTestPerson("Original", "Name");
        
        Person updatedPerson = new Person();
        updatedPerson.setFirstName("Updated");
        updatedPerson.setLastName("Person");
        
        HttpEntity<Person> request = new HttpEntity<>(updatedPerson, headers);
        
        // Act
        ResponseEntity<Person> response = restTemplate.exchange(
                getBaseUrl() + "/{id}", HttpMethod.PUT, request, Person.class, savedPerson.getId());
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(savedPerson.getId(), response.getBody().getId());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Person", response.getBody().getLastName());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent person")
    void updatePerson_NotExists() {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setFirstName("Updated");
        updatedPerson.setLastName("Person");
        
        HttpEntity<Person> request = new HttpEntity<>(updatedPerson, headers);
        
        // Act
        ResponseEntity<Person> response = restTemplate.exchange(
                getBaseUrl() + "/{id}", HttpMethod.PUT, request, Person.class, 999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should delete person when exists")
    void deletePerson_Exists() {
        // Arrange
        Person savedPerson = createTestPerson("Delete", "Me");
        
        // Act
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getBaseUrl() + "/{id}", HttpMethod.DELETE, null, Void.class, savedPerson.getId());
        
        // Assert delete response
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        
        // Verify person is deleted
        ResponseEntity<Person> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Person.class, savedPerson.getId());
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent person")
    void deletePerson_NotExists() {
        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/{id}", HttpMethod.DELETE, null, Void.class, 999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle invalid person data")
    void createPerson_InvalidData() {
        // Arrange - Create person with invalid data (assuming validation is in place)
        Person invalidPerson = new Person();
        // Leave required fields empty
        
        HttpEntity<Person> request = new HttpEntity<>(invalidPerson, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class);
        
        // Assert - Expecting 400 Bad Request if validation is implemented
        // If validation is not implemented, this might need adjustment
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Helper method to create and save a test person
    private Person createTestPerson(String firstName, String lastName) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        return personRepository.save(person);
    }
}
