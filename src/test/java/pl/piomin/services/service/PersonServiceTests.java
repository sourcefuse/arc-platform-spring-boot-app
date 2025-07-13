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

    @BeforeEach
    void setUp() {
        testPerson = Instancio.create(Person.class);
        testPerson.setId(1L);
    }

    @Test
    @DisplayName("Should add a new person and return it with ID")
    void shouldAddPerson() {
        // Arrange
        Person inputPerson = Instancio.create(Person.class);
        inputPerson.setId(null); // New person doesn't have ID yet
        
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        // Act
        Person result = personService.add(inputPerson);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(personRepository).save(inputPerson);
    }

    @Test
    @DisplayName("Should return all persons")
    void shouldFindAllPersons() {
        // Arrange
        List<Person> persons = Arrays.asList(
            testPerson,
            Instancio.of(Person.class).set(field -> field.name("id"), 2L).create()
        );
        
        when(personRepository.findAll()).thenReturn(persons);

        // Act
        List<Person> result = personService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(personRepository).findAll();
    }

    @Test
    @DisplayName("Should return person by ID when exists")
    void shouldFindPersonById() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));

        // Act
        Optional<Person> result = personService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(personRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty Optional when person not found")
    void shouldReturnEmptyOptionalWhenPersonNotFound() {
        // Arrange
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Person> result = personService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(personRepository).findById(99L);
    }

    @Test
    @DisplayName("Should delete person by ID")
    void shouldDeletePerson() {
        // Arrange
        doNothing().when(personRepository).deleteById(anyLong());

        // Act
        personService.delete(1L);

        // Assert
        verify(personRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should update person when exists")
    void shouldUpdatePersonWhenExists() {
        // Arrange
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(1L);
        updatedPerson.setFirstName("UpdatedName");
        
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);

        // Act
        Optional<Person> result = personService.update(1L, updatedPerson);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("UpdatedName", result.get().getFirstName());
        verify(personRepository).findById(1L);
        verify(personRepository).save(any(Person.class));
    }

    @Test
    @DisplayName("Should return empty Optional when updating non-existent person")
    void shouldReturnEmptyOptionalWhenUpdatingNonExistentPerson() {
        // Arrange
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(99L);
        
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Person> result = personService.update(99L, updatedPerson);

        // Assert
        assertFalse(result.isPresent());
        verify(personRepository).findById(99L);
        verify(personRepository, never()).save(any(Person.class));
    }
}
