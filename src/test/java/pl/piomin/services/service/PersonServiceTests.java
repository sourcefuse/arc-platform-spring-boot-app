package pl.piomin.services.service;

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
    @DisplayName("Should add a new person and return with generated ID")
    void shouldAddPerson() {
        // Arrange
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);
        
        // Act
        Person result = personService.add(testPerson);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    @DisplayName("Should find person by ID when person exists")
    void shouldFindPersonById() {
        // Arrange
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(testPerson));
        
        // Act
        Person result = personService.findById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(testPerson.getId(), result.getId());
        assertEquals(testPerson.getFirstName(), result.getFirstName());
        assertEquals(testPerson.getLastName(), result.getLastName());
        verify(personRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when person not found by ID")
    void shouldThrowExceptionWhenPersonNotFound() {
        // Arrange
        when(personRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(PersonNotFoundException.class, () -> {
            personService.findById(1L);
        });
        
        assertTrue(exception.getMessage().contains("Person not found"));
        verify(personRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should find all persons")
    void shouldFindAllPersons() {
        // Arrange
        Person person1 = Instancio.create(Person.class);
        Person person2 = Instancio.create(Person.class);
        List<Person> persons = Arrays.asList(person1, person2);
        
        when(personRepository.findAll()).thenReturn(persons);
        
        // Act
        List<Person> result = personService.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(personRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update existing person")
    void shouldUpdatePerson() {
        // Arrange
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(1L);
        updatedPerson.setFirstName("UpdatedFirstName");
        
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);
        
        // Act
        Person result = personService.update(updatedPerson);
        
        // Assert
        assertNotNull(result);
        assertEquals("UpdatedFirstName", result.getFirstName());
        verify(personRepository, times(1)).findById(1L);
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent person")
    void shouldThrowExceptionWhenUpdatingNonExistentPerson() {
        // Arrange
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(1L);
        
        when(personRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(PersonNotFoundException.class, () -> {
            personService.update(updatedPerson);
        });
        
        assertTrue(exception.getMessage().contains("Person not found"));
        verify(personRepository, times(1)).findById(1L);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    @DisplayName("Should delete person by ID")
    void shouldDeletePersonById() {
        // Arrange
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(testPerson));
        doNothing().when(personRepository).deleteById(anyLong());
        
        // Act
        personService.delete(1L);
        
        // Assert
        verify(personRepository, times(1)).findById(1L);
        verify(personRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent person")
    void shouldThrowExceptionWhenDeletingNonExistentPerson() {
        // Arrange
        when(personRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(PersonNotFoundException.class, () -> {
            personService.delete(1L);
        });
        
        assertTrue(exception.getMessage().contains("Person not found"));
        verify(personRepository, times(1)).findById(1L);
        verify(personRepository, never()).deleteById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"John", "Alice", "Bob"})
    @DisplayName("Should find persons by first name")
    void shouldFindPersonsByFirstName(String firstName) {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setFirstName(firstName);
        List<Person> persons = Arrays.asList(person);
        
        when(personRepository.findByFirstName(firstName)).thenReturn(persons);
        
        // Act
        List<Person> result = personService.findByFirstName(firstName);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(firstName, result.get(0).getFirstName());
        verify(personRepository, times(1)).findByFirstName(firstName);
    }
}