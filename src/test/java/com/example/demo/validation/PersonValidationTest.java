package com.example.demo.validation;

import com.example.demo.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate a valid person")
    void validPerson() {
        // Given
        Person person = new Person(1L, "John", "Doe", 30);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject empty first name")
    void emptyFirstName() {
        // Given
        Person person = new Person(1L, "", "Doe", 30);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    @DisplayName("Should reject null first name")
    void nullFirstName() {
        // Given
        Person person = new Person(1L, null, "Doe", 30);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    @DisplayName("Should reject empty last name")
    void emptyLastName() {
        // Given
        Person person = new Person(1L, "John", "", 30);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    @Test
    @DisplayName("Should reject null last name")
    void nullLastName() {
        // Given
        Person person = new Person(1L, "John", null, 30);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    @Test
    @DisplayName("Should reject negative age")
    void negativeAge() {
        // Given
        Person person = new Person(1L, "John", "Doe", -1);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("age");
    }

    @Test
    @DisplayName("Should reject age over maximum")
    void ageOverMaximum() {
        // Given
        Person person = new Person(1L, "John", "Doe", 151);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("age");
    }

    @Test
    @DisplayName("Should reject multiple invalid fields")
    void multipleInvalidFields() {
        // Given
        Person person = new Person(1L, "", "", -1);

        // When
        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // Then
        assertThat(violations).hasSize(3);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("firstName", "lastName", "age");
    }
}
