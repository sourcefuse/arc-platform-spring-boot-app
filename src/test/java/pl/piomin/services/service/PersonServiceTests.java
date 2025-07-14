package pl.piomin.services.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

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
    @DisplayName("Should add a new person successfully")
    void shouldAddPerson() {
        // Arrange
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        // Act
        Person result = personService.add(testPerson);

        // Assert
        assertNotNull(result);
        assertEquals(testPerson.getId(), result.getId());
        assertEquals(testPerson.getFirstName(), result.getFirstName());
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    @DisplayName("Should find all persons")
    void shouldFindAllPersons() {
        // Arrange
        when(personRepository.findAll()).thenReturn(testPersons);

        // Act
        List<Person> result = personService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testPersons.get(0).getId(), result.get(0).getId());
        verify(personRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find person by ID when person exists")
    void shouldFindPersonById() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));

        // Act
        Optional<Person> result = personService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPerson.getId(), result.get().getId());
        assertEquals(testPerson.getFirstName(), result.get().getFirstName());
        verify(personRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when person does not exist")
    void shouldReturnEmptyOptionalWhenPersonDoesNotExist() {
        // Arrange
        when(personRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Person> result = personService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(personRepository, times(1)).findById(999L);
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

        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);

        // Act
        Person result = personService.update(updatedPerson);

        // Assert
        assertNotNull(result);
        assertEquals(updatedPerson.getId(), result.getId());
        assertEquals("John-Updated", result.getFirstName());
        assertEquals("Doe-Updated", result.getLastName());
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    @DisplayName("Should delete person successfully")
    void shouldDeletePerson() {
        // Arrange
        doNothing().when(personRepository).deleteById(anyLong());

        // Act
        personService.delete(1L);

        // Assert
        verify(personRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle null person when adding")
    void shouldHandleNullPersonWhenAdding() {
        // Arrange
        when(personRepository.save(null)).thenThrow(IllegalArgumentException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> personService.add(null));
    }

    @Test
    @DisplayName("Should handle null person when updating")
    void shouldHandleNullPersonWhenUpdating() {
        // Arrange
        when(personRepository.save(null)).thenThrow(IllegalArgumentException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> personService.update(null));
    }
}
