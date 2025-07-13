package pl.piomin.services.controller;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.piomin.services.domain.Person;
import pl.piomin.services.service.PersonService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonControllerUnitTests {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonController personController;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = Instancio.create(Person.class);
        testPerson.setId(1L);
    }

    @Test
    @DisplayName("Should create a new person and return it with ID")
    void shouldCreatePerson() {
        // Arrange
        Person inputPerson = Instancio.create(Person.class);
        inputPerson.setId(null); // New person doesn't have ID yet
        
        when(personService.add(any(Person.class))).thenReturn(testPerson);

        // Act
        ResponseEntity<Person> response = personController.add(inputPerson);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(personService).add(inputPerson);
    }

    @Test
    @DisplayName("Should return all persons")
    void shouldFindAllPersons() {
        // Arrange
        List<Person> persons = Arrays.asList(
            testPerson,
            Instancio.of(Person.class).set(field -> field.name("id"), 2L).create()
        );
        
        when(personService.findAll()).thenReturn(persons);

        // Act
        ResponseEntity<List<Person>> response = personController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(personService).findAll();
    }

    @Test
    @DisplayName("Should return person by ID when exists")
    void shouldFindPersonById() {
        // Arrange
        when(personService.findById(1L)).thenReturn(Optional.of(testPerson));

        // Act
        ResponseEntity<Person> response = personController.findById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(personService).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when person not found")
    void shouldReturn404WhenPersonNotFound() {
        // Arrange
        when(personService.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Person> response = personController.findById(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(personService).findById(99L);
    }

    @Test
    @DisplayName("Should delete person and return 204 No Content")
    void shouldDeletePerson() {
        // Arrange
        doNothing().when(personService).delete(anyLong());

        // Act
        ResponseEntity<Void> response = personController.delete(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(personService).delete(1L);
    }

    @Test
    @DisplayName("Should update person and return updated entity")
    void shouldUpdatePerson() {
        // Arrange
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(1L);
        updatedPerson.setFirstName("UpdatedName");
        
        when(personService.update(eq(1L), any(Person.class))).thenReturn(Optional.of(updatedPerson));

        // Act
        ResponseEntity<Person> response = personController.update(1L, updatedPerson);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UpdatedName", response.getBody().getFirstName());
        verify(personService).update(eq(1L), any(Person.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent person")
    void shouldReturn404WhenUpdatingNonExistentPerson() {
        // Arrange
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(99L);
        
        when(personService.update(eq(99L), any(Person.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Person> response = personController.update(99L, updatedPerson);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(personService).update(eq(99L), any(Person.class));
    }
}
