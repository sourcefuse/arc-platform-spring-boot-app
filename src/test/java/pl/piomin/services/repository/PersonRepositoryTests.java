package pl.piomin.services.repository;

import org.instancio.Instancio;
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

    @Test
    @DisplayName("Should save person")
    void shouldSavePerson() {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(null);
        
        // Act
        Person savedPerson = personRepository.save(person);
        
        // Assert
        assertNotNull(savedPerson);
        assertNotNull(savedPerson.getId());
        assertEquals(person.getFirstName(), savedPerson.getFirstName());
        assertEquals(person.getLastName(), savedPerson.getLastName());
    }

    @Test
    @DisplayName("Should find person by ID")
    void shouldFindPersonById() {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(null);
        person = entityManager.persistAndFlush(person);
        
        // Act
        Optional<Person> foundPerson = personRepository.findById(person.getId());
        
        // Assert
        assertTrue(foundPerson.isPresent());
        assertEquals(person.getId(), foundPerson.get().getId());
        assertEquals(person.getFirstName(), foundPerson.get().getFirstName());
        assertEquals(person.getLastName(), foundPerson.get().getLastName());
    }

    @Test
    @DisplayName("Should find all persons")
    void shouldFindAllPersons() {
        // Arrange
        Person person1 = Instancio.create(Person.class);
        person1.setId(null);
        entityManager.persist(person1);
        
        Person person2 = Instancio.create(Person.class);
        person2.setId(null);
        entityManager.persist(person2);
        
        entityManager.flush();
        
        // Act
        List<Person> persons = personRepository.findAll();
        
        // Assert
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals(person1.getFirstName())));
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals(person2.getFirstName())));
    }

    @Test
    @DisplayName("Should find persons by first name")
    void shouldFindPersonsByFirstName() {
        // Arrange
        String firstName = "TestFirstName";
        
        Person person1 = Instancio.create(Person.class);
        person1.setId(null);
        person1.setFirstName(firstName);
        entityManager.persist(person1);
        
        Person person2 = Instancio.create(Person.class);
        person2.setId(null);
        person2.setFirstName(firstName);
        entityManager.persist(person2);
        
        Person person3 = Instancio.create(Person.class);
        person3.setId(null);
        person3.setFirstName("DifferentName");
        entityManager.persist(person3);
        
        entityManager.flush();
        
        // Act
        List<Person> persons = personRepository.findByFirstName(firstName);
        
        // Assert
        assertNotNull(persons);
        assertEquals(2, persons.size());
        assertTrue(persons.stream().allMatch(p -> p.getFirstName().equals(firstName)));
    }

    @Test
    @DisplayName("Should update person")
    void shouldUpdatePerson() {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(null);
        person = entityManager.persistAndFlush(person);
        
        // Act
        person.setFirstName("UpdatedFirstName");
        person.setLastName("UpdatedLastName");
        Person updatedPerson = personRepository.save(person);
        
        // Assert
        assertEquals("UpdatedFirstName", updatedPerson.getFirstName());
        assertEquals("UpdatedLastName", updatedPerson.getLastName());
        
        // Verify in database
        Person retrievedPerson = entityManager.find(Person.class, person.getId());
        assertEquals("UpdatedFirstName", retrievedPerson.getFirstName());
        assertEquals("UpdatedLastName", retrievedPerson.getLastName());
    }

    @Test
    @DisplayName("Should delete person")
    void shouldDeletePerson() {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(null);
        person = entityManager.persistAndFlush(person);
        
        // Act
        personRepository.deleteById(person.getId());
        
        // Assert
        Person retrievedPerson = entityManager.find(Person.class, person.getId());
        assertNull(retrievedPerson);
    }
}