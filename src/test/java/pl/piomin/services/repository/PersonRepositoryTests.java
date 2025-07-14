package pl.piomin.services.repository;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.piomin.services.domain.Person;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PersonRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        // Create and persist test data
        testPerson = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), "John")
                .set(field -> field.name("lastName"), "Doe")
                .set(field -> field.name("age"), 30)
                .create();

        entityManager.persist(testPerson);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find person by ID")
    void shouldFindPersonById() {
        // Act
        Optional<Person> found = personRepository.findById(testPerson.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(testPerson.getFirstName(), found.get().getFirstName());
        assertEquals(testPerson.getLastName(), found.get().getLastName());
    }

    @Test
    @DisplayName("Should find all persons")
    void shouldFindAllPersons() {
        // Arrange
        Person secondPerson = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), "Jane")
                .set(field -> field.name("lastName"), "Smith")
                .set(field -> field.name("age"), 25)
                .create();

        entityManager.persist(secondPerson);
        entityManager.flush();

        // Act
        List<Person> persons = personRepository.findAll();

        // Assert
        assertEquals(2, persons.size());
    }

    @Test
    @DisplayName("Should save new person")
    void shouldSaveNewPerson() {
        // Arrange
        Person newPerson = Instancio.of(Person.class)
                .ignore(field -> field.name("id"))
                .set(field -> field.name("firstName"), "Alice")
                .set(field -> field.name("lastName"), "Johnson")
                .set(field -> field.name("age"), 35)
                .create();

        // Act
        Person saved = personRepository.save(newPerson);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getFirstName());
        assertEquals("Johnson", saved.getLastName());
    }

    @Test
    @DisplayName("Should update existing person")
    void shouldUpdateExistingPerson() {
        // Arrange
        testPerson.setFirstName("John-Updated");
        testPerson.setAge(31);

        // Act
        Person updated = personRepository.save(testPerson);

        // Assert
        assertEquals("John-Updated", updated.getFirstName());
        assertEquals(31, updated.getAge());

        // Verify in database
        Person fromDb = entityManager.find(Person.class, testPerson.getId());
        assertEquals("John-Updated", fromDb.getFirstName());
        assertEquals(31, fromDb.getAge());
    }

    @Test
    @DisplayName("Should delete person")
    void shouldDeletePerson() {
        // Act
        personRepository.deleteById(testPerson.getId());

        // Assert
        Person fromDb = entityManager.find(Person.class, testPerson.getId());
        assertNull(fromDb);
    }

    @Test
    @DisplayName("Should return empty optional when person not found")
    void shouldReturnEmptyOptionalWhenPersonNotFound() {
        // Act
        Optional<Person> result = personRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }
}
