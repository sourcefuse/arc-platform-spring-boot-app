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
import pl.piomin.services.repository.PersonRepository;

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
    private PersonRepository personRepository;

    @InjectMocks
    private PersonController personController;

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
        ResponseEntity<Person> response = personController.createPerson(testPerson);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
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
        ResponseEntity<List<Person>> response = personController.getAllPersons();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(personRepository).findAll();
    }

    @Test
    @DisplayName("Should return person by ID when exists")
    void getPersonById_Exists() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        
        // Act
        ResponseEntity<Person> response = personController.getPersonById(1L);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(personRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return not found when person doesn't exist")
    void getPersonById_NotExists() {
        // Arrange
        when(personRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<Person> response = personController.getPersonById(999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
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
        ResponseEntity<Person> response = personController.updatePerson(1L, updatedPerson);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UpdatedName", response.getBody().getFirstName());
        assertEquals("UpdatedLastName", response.getBody().getLastName());
        verify(personRepository).existsById(1L);
        verify(personRepository).save(any(Person.class));
    }

    @Test
    @DisplayName("Should return not found when updating non-existent person")
    void updatePerson_NotExists() {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setId(999L);
        
        when(personRepository.existsById(999L)).thenReturn(false);
        
        // Act
        ResponseEntity<Person> response = personController.updatePerson(999L, updatedPerson);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
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
        ResponseEntity<Void> response = personController.deletePerson(1L);
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(personRepository).existsById(1L);
        verify(personRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent person")
    void deletePerson_NotExists() {
        // Arrange
        when(personRepository.existsById(999L)).thenReturn(false);
        
        // Act
        ResponseEntity<Void> response = personController.deletePerson(999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(personRepository).existsById(999L);
        verify(personRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle exception during save operation")
    void createPerson_Exception() {
        // Arrange
        when(personRepository.save(any(Person.class))).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            personController.createPerson(testPerson);
        });
        
        assertEquals("Database error", exception.getMessage());
        verify(personRepository).save(any(Person.class));
    }
}
