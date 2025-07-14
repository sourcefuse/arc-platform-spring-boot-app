package pl.piomin.services.controller;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    private List<Person> testPersons;

    @BeforeEach
    void setUp() {
        // Create test data using Instancio
        testPerson = Instancio.of(Person.class)
                .set(field -> field.name("id"), 1L)
                .set(field -> field.name("firstName"), "John")
                .set(field -> field.name("lastName"), "Doe")
                .set(field -> field.name("age"), 30)
                .create();

        Person person2 = Instancio.of(Person.class)
                .set(field -> field.name("id"), 2L)
                .set(field -> field.name("firstName"), "Jane")
                .set(field -> field.name("lastName"), "Smith")
                .set(field -> field.name("age"), 25)
                .create();

        testPersons = Arrays.asList(testPerson, person2);
    }

    @Test
    @DisplayName("Should create a new person successfully")
    void shouldCreatePerson() {
        // Arrange
        when(personService.add(any(Person.class))).thenReturn(testPerson);

        // Act
        Person result = personController.add(testPerson);

        // Assert
        assertNotNull(result);
        assertEquals(testPerson.getId(), result.getId());
        assertEquals(testPerson.getFirstName(), result.getFirstName());
        assertEquals(testPerson.getLastName(), result.getLastName());
        verify(personService, times(1)).add(any(Person.class));
    }

    @Test
    @DisplayName("Should return all persons")
    void shouldFindAllPersons() {
        // Arrange
        when(personService.findAll()).thenReturn(testPersons);

        // Act
        List<Person> result = personController.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testPersons.get(0).getId(), result.get(0).getId());
        assertEquals(testPersons.get(1).getId(), result.get(1).getId());
        verify(personService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find person by ID when person exists")
    void shouldFindPersonById() {
        // Arrange
        when(personService.findById(1L)).thenReturn(Optional.of(testPerson));

        // Act
        ResponseEntity<Person> response = personController.findById(1L);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testPerson.getId(), response.getBody().getId());
        verify(personService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when person does not exist")
    void shouldReturnNotFoundWhenPersonDoesNotExist() {
        // Arrange
        when(personService.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Person> response = personController.findById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(personService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should update person successfully")
    void shouldUpdatePerson() {
        // Arrange
        Person updatedPerson = Instancio.of(Person.class)
                .set(field -> field.name("id"), 1L)
                .set(field -> field.name("firstName"), "John-Updated")
                .set(field -> field.name("lastName"), "Doe-Updated")
                .set(field -> field.name("age"), 31)
                .create();

        when(personService.update(any(Person.class))).thenReturn(updatedPerson);

        // Act
        Person result = personController.update(updatedPerson);

        // Assert
        assertNotNull(result);
        assertEquals(updatedPerson.getId(), result.getId());
        assertEquals("John-Updated", result.getFirstName());
        assertEquals("Doe-Updated", result.getLastName());
        assertEquals(31, result.getAge());
        verify(personService, times(1)).update(any(Person.class));
    }

    @Test
    @DisplayName("Should delete person successfully")
    void shouldDeletePerson() {
        // Arrange
        doNothing().when(personService).delete(anyLong());

        // Act
        personController.delete(1L);

        // Assert
        verify(personService, times(1)).delete(1L);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("Should handle multiple IDs when finding persons")
    void shouldFindPersonByMultipleIds(long id) {
        // Arrange
        Person person = Instancio.of(Person.class)
                .set(field -> field.name("id"), id)
                .create();
        when(personService.findById(id)).thenReturn(Optional.of(person));

        // Act
        ResponseEntity<Person> response = personController.findById(id);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(id, response.getBody().getId());
        verify(personService, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should handle null person when creating")
    void shouldHandleNullPersonWhenCreating() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> personController.add(null));
        verify(personService, never()).add(any());
    }
}
