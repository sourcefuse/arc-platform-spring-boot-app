package com.example.demo.repository;

import com.example.demo.model.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    @Test
    @DisplayName("Should find person by ID when exists")
    void findById() {
        // Given
        Person savedPerson = entityManager.persistAndFlush(new Person(null, "John", "Doe", 30));

        // When
        Optional<Person> foundPerson = personRepository.findById(savedPerson.getId());

        // Then
        assertTrue(foundPerson.isPresent());
        assertEquals("John", foundPerson.get().getFirstName());
        assertEquals("Doe", foundPerson.get().getLastName());
    }

    @Test
    @DisplayName("Should return empty optional when person not found")
    void findByIdNotFound() {
        // When
        Optional<Person> result = personRepository.findById(999L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all persons")
    void findAll() {
        // Given
        entityManager.persistAndFlush(new Person(null, "John", "Doe", 30));
        entityManager.persistAndFlush(new Person(null, "Jane", "Smith", 25));

        // When
        List<Person> persons = personRepository.findAll();

        // Then
        assertThat(persons).hasSize(2);
        assertThat(persons).extracting(Person::getFirstName).containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    @DisplayName("Should save a new person")
    void save() {
        // Given
        Person personToSave = new Person(null, "New", "Person", 22);

        // When
        Person savedPerson = personRepository.save(personToSave);

        // Then
        assertNotNull(savedPerson.getId());
        assertEquals("New", savedPerson.getFirstName());
        assertEquals("Person", savedPerson.getLastName());
        assertEquals(22, savedPerson.getAge());
    }

    @Test
    @DisplayName("Should update an existing person")
    void update() {
        // Given
        Person savedPerson = entityManager.persistAndFlush(new Person(null, "John", "Doe", 30));
        savedPerson.setLastName("Updated");
        savedPerson.setAge(31);

        // When
        Person updatedPerson = personRepository.save(savedPerson);

        // Then
        assertEquals(savedPerson.getId(), updatedPerson.getId());
        assertEquals("Updated", updatedPerson.getLastName());
        assertEquals(31, updatedPerson.getAge());
    }

    @Test
    @DisplayName("Should delete a person")
    void delete() {
        // Given
        Person savedPerson = entityManager.persistAndFlush(new Person(null, "John", "Doe", 30));

        // When
        personRepository.deleteById(savedPerson.getId());
        Optional<Person> result = personRepository.findById(savedPerson.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find persons by age range")
    void findByAgeBetween() {
        // Given
        entityManager.persistAndFlush(new Person(null, "John", "Doe", 30));
        entityManager.persistAndFlush(new Person(null, "Jane", "Smith", 25));
        entityManager.persistAndFlush(new Person(null, "Bob", "Johnson", 40));

        // When
        List<Person> result = personRepository.findByAgeBetween(25, 35);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Person::getFirstName).containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    @DisplayName("Should find persons by last name")
    void findByLastName() {
        // Given
        entityManager.persistAndFlush(new Person(null, "John", "Doe", 30));
        entityManager.persistAndFlush(new Person(null, "Jane", "Smith", 25));
        entityManager.persistAndFlush(new Person(null, "Bob", "Doe", 40));

        // When
        List<Person> result = personRepository.findByLastName("Doe");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Person::getFirstName).containsExactlyInAnyOrder("John", "Bob");
    }
}
