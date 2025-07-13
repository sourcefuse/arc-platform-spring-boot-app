package pl.piomin.services.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.piomin.services.domain.Person;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonControllerUnitTest {

    @InjectMocks
    private PersonController personController;

    @Test
    void testAddPerson() {
        Person person = new Person();
        person.setId(1L);
        Person result = personController.add(person);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindAllPersons() {
        List<Person> persons = personController.findAll();
        assertNotNull(persons);
    }

    @Test
    void testFindPersonById() {
        Person person = new Person();
        person.setId(1L);
        personController.add(person);
        Person result = personController.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testDeletePerson() {
        Person person = new Person();
        person.setId(1L);
        personController.add(person);
        personController.delete(1L);
        assertThrows(RuntimeException.class, () -> personController.findById(1L));
    }
}