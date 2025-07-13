package com.example.demo.service;

import com.example.demo.model.Person;
import com.example.demo.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person person1;
    private Person person2;

    @BeforeEach
    void setUp() {
        person1 = new Person(1L, "John", "Doe", 30);
        person2 = new Person(2L, "Jane", "Smith", 25);
    }

    @Test
    @DisplayName("Should find all persons")
    void findAll() {
        // Given
        List<Person> expectedPersons = Arrays.asList(person1, person2);
        when(personRepository.findAll()).thenReturn(expectedPersons);

        // When
        List<Person> actualPersons = personService.findAll();

        // Then
        assertThat(actualPersons).hasSize(2);
        assertThat(actualPersons).containsExactlyElementsOf(expectedPersons);
        verify(personRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no persons exist")
    void findAllEmpty() {
        // Given
        when(personRepository.findAll()).thenReturn(List.of());

        // When
        List<Person> actualPersons = personService.findAll();

        // Then
        assertThat(actualPersons).isEmpty();
        verify(personRepository).findAll();
    }

    @Test
    @DisplayName("Should find person by ID when exists")
    void findById() {
        // Given
        when(personRepository.findById(1L)).thenReturn(Optional.of(person1));

        // When
        Optional<Person> result = personService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(person1, result.get());
        verify(personRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when person not found")
    void findByIdNotFound() {
        // Given
        when(personRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Person> result = personService.findById(999L);

        // Then
        assertTrue(result.isEmpty());
        verify(personRepository).findById(999L);
    }

    @Test
    @DisplayName("Should add a new person")
    void add() {
        // Given
        Person personToAdd = new Person(null, "New", "Person", 22);
        Person savedPerson = new Person(3L, "New", "Person", 22);
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

        // When
        Person result = personService.add(personToAdd);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New", result.getFirstName());
        verify(personRepository).save(personToAdd);
    }

    @Test
    @DisplayName("Should update an existing person")
    void update() {
        // Given
        Person personToUpdate = new Person(1L, "John", "Updated", 31);
        when(personRepository.save(any(Person.class))).thenReturn(personToUpdate);

        // When
        Person result = personService.update(personToUpdate);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getLastName());
        assertEquals(31, result.getAge());
        verify(personRepository).save(personToUpdate);
    }

    @Test
    @DisplayName("Should delete a person by ID")
    void delete() {
        // Given
        doNothing().when(personRepository).deleteById(1L);

        // When
        personService.delete(1L);

        // Then
        verify(personRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should find persons by age range")
    void findByAgeRange() {
        // Given
        int minAge = 25;
        int maxAge = 30;
        List<Person> expectedPersons = Arrays.asList(person1, person2);
        when(personRepository.findByAgeBetween(minAge, maxAge)).thenReturn(expectedPersons);

        // When
        List<Person> result = personService.findByAgeRange(minAge, maxAge);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedPersons);
        verify(personRepository).findByAgeBetween(minAge, maxAge);
    }

    @Test
    @DisplayName("Should find persons by last name")
    void findByLastName() {
        // Given
        String lastName = "Doe";
        when(personRepository.findByLastName(lastName)).thenReturn(List.of(person1));

        // When
        List<Person> result = personService.findByLastName(lastName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).isEqualTo(lastName);
        verify(personRepository).findByLastName(lastName);
    }
}
