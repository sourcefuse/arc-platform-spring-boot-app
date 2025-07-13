package pl.piomin.services.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pl.piomin.services.domain.Person;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerIntegrationTest {

    private static final String API_PATH = "/api/v1";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void addPerson() {
        Person person = new Person();
        person.setId(1L);
        Person result = restTemplate.postForObject(API_PATH, person, Person.class);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @Order(2)
    void findAllPersons() {
        Person[] persons = restTemplate.getForObject(API_PATH, Person[].class);
        assertTrue(persons.length > 0);
    }

    @Test
    @Order(3)
    void findPersonById() {
        Person result = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @Order(4)
    void deletePerson() {
        restTemplate.delete(API_PATH + "/{id}", 1L);
        Person result = restTemplate.getForObject(API_PATH + "/{id}", Person.class, 1L);
        assertNull(result);
    }
}