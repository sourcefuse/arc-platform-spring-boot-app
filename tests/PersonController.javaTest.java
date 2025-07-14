/**
 * AI-Generated Test Cases
 * Original File: PersonController.java
 * Generated: 2025-07-14T16:17:44.190Z
 * Framework: junit
 * 
 * Setup Instructions:
 * Ensure that the Spring Boot application is configured correctly with the necessary dependencies in the pom.xml. The application should be running in a test environment with a test database setup.
 * 
 * Run with: mvn test
 */

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonControllerTest {
    
    @BeforeEach
    void setUp() {
        // Setup for each test
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup after each test
    }

    /**
     * Test adding a new person via the API
     * Type: integration
     * Priority: high
     */
    @Test
    @DisplayName("Test adding a new person via the API")
    void test_addPerson() {
        
        // Test execution
        @Test
@Order(1)
void test_addPerson() {
    Person obj = restTemplate.postForObject(API_PATH, Instancio.create(Person.class), Person.class);
    assertNotNull(obj);
    assertEquals(1, obj.getId());
}
        
    }

    /**
     * Test retrieving all persons from the API
     * Type: integration
     * Priority: medium
     */
    @Test
    @DisplayName("Test retrieving all persons from the API")
    void test_findAllPersons() {
        
        // Test execution
        @Test
@Order(2)
void test_findAllPersons() {
    Person[] objs = restTemplate.getForObject(API_PATH, Person[].class);
    assertTrue(objs.length > 0);
}
        
    }

    /**
     * Test retrieving a person by ID from the API
     * Type: integration
     * Priority: high
     */
    @Test
    @DisplayName("Test retrieving a person by ID from the API")
    void test_findPersonById() {
        
        // Test execution
        @Test
@Order(2)
void test_findPersonById() {
    Person obj = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
    assertNotNull(obj);
    assertEquals(1, obj.getId());
}
        
    }

    /**
     * Test deleting a person by ID via the API
     * Type: integration
     * Priority: high
     */
    @Test
    @DisplayName("Test deleting a person by ID via the API")
    void test_deletePerson() {
        
        // Test execution
        @Test
@Order(3)
void test_deletePerson() {
    restTemplate.delete(API_PATH + "/{id}", 1L);
    Person obj = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
    assertNull(obj.getId());
}
        
    }

}