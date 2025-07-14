package pl.piomin.services.domain;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PersonValidationTests {

    private Validator validator;
    private Person validPerson;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validPerson = Instancio.create(Person.class);
        validPerson.setId(1L);
    }

    @Test
    @DisplayName("Should validate valid person")
    void shouldValidateValidPerson() {
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should validate person with null first name")
    void shouldValidatePersonWithNullFirstName() {
        // Arrange
        validPerson.setFirstName(null);
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("firstName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should validate person with null last name")
    void shouldValidatePersonWithNullLastName() {
        // Arrange
        validPerson.setLastName(null);
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("lastName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should validate person with empty first name")
    void shouldValidatePersonWithEmptyFirstName() {
        // Arrange
        validPerson.setFirstName("");
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("firstName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should validate person with empty last name")
    void shouldValidatePersonWithEmptyLastName() {
        // Arrange
        validPerson.setLastName("");
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("lastName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should validate person with too long first name")
    void shouldValidatePersonWithTooLongFirstName() {
        // Arrange
        validPerson.setFirstName("A".repeat(101)); // Assuming max length is 100
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("firstName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should validate person with too long last name")
    void shouldValidatePersonWithTooLongLastName() {
        // Arrange
        validPerson.setLastName("A".repeat(101)); // Assuming max length is 100
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("lastName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should validate person with multiple violations")
    void shouldValidatePersonWithMultipleViolations() {
        // Arrange
        validPerson.setFirstName(null);
        validPerson.setLastName(null);
        
        // Act
        Set<ConstraintViolation<Person>> violations = validator.validate(validPerson);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }
}