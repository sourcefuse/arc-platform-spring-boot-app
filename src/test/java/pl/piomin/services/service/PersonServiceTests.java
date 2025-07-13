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
import pl.piomin.services.exception.PersonNotFoundException;
import pl.piomin.services.repository.PersonRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("Should create a new person")
    void createPerson() {
        // Arrange
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);
        
        // Act
        Person result = personService.createPerson(testPerson);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(personRepository).save(any(Person.class));
    }

    @Test
    @DisplayName("Should return all persons")
    void getAllPersons() {
        // Arrange
        Person person2 = Instancio.create(Person.class);
        person2.setId(2L);
        List<Person> persons = Arrays.asList(testPerson, person2);
        when(personRepository.findAll()).thenReturn(persons);
        
        // Act
        List<Person> result = personService.getAllPersons();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(personRepository).findAll();
    }

    @Test
    @DisplayName("Should return person by ID when exists")
    void getPersonById_Exists() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        
        // Act
        Person result = personService.getPersonById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(personRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when person doesn't exist")
    void getPersonById_NotExists() {
        // Arrange
        when(personRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(PersonNotFoundException.class, () -> {
            personService.getPersonById(999L);
        });
        verify(personRepository).findById(999L);
    }

    @Test
    @DisplayName("Should update person when exists")
    void updatePerson_Exists() {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setId(1L);
        updatedPerson.setFirstName("UpdatedName");
        updatedPerson.setLastName("UpdatedLastName");
        
        when(personRepository.existsById(1L)).thenReturn(true);
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);
        
        // Act
        Person result = personService.updatePerson(1L, updatedPerson);
        
        // Assert
        assertNotNull(result);
        assertEquals("UpdatedName", result.getFirstName());
        assertEquals("UpdatedLastName", result.getLastName());
        verify(personRepository).existsById(1L);
        verify(personRepository).save(any(Person.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent person")
    void updatePerson_NotExists() {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setId(999L);
        
        when(personRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(PersonNotFoundException.class, () -> {
            personService.updatePerson(999L, updatedPerson);
        });
        verify(personRepository).existsById(999L);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    @DisplayName("Should delete person when exists")
    void deletePerson_Exists() {
        // Arrange
        when(personRepository.existsById(1L)).thenReturn(true);
        doNothing().when(personRepository).deleteById(1L);
        
        // Act
        personService.deletePerson(1L);
        
        // Assert
        verify(personRepository).existsById(1L);
        verify(personRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent person")
    void deletePerson_NotExists() {
        // Arrange
        when(personRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(PersonNotFoundException.class, () -> {
            personService.deletePerson(999L);
        });
        verify(personRepository).existsById(999L);
        verify(personRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should find persons by last name")
    void findByLastName() {
        // Arrange
        String lastName = "Smith";
        Person person1 = new Person();
        person1.setId(1L);
        person1.setFirstName("John");
        person1.setLastName(lastName);
        
        Person person2 = new Person();
        person2.setId(2L);
        person2.setFirstName("Jane");
        person2.setLastName(lastName);
        
        List<Person> persons = Arrays.asList(person1, person2);
        when(personRepository.findByLastName(lastName)).thenReturn(persons);
        
        // Act
        List<Person> result = personService.findByLastName(lastName);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(lastName, result.get(0).getLastName());
        assertEquals(lastName, result.get(1).getLastName());
        verify(personRepository).findByLastName(lastName);
    }

    @Test
    @DisplayName("Should return empty list when no persons with given last name")
    void findByLastName_NoResults() {
        // Arrange
        String lastName = "NonExistent";
        when(personRepository.findByLastName(lastName)).thenReturn(Arrays.asList());
        
        // Act
        List<Person> result = personService.findByLastName(lastName);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(personRepository).findByLastName(lastName);
    }
}
