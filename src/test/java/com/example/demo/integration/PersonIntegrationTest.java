package com.example.demo.integration;

import com.example.demo.model.Person;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PersonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_PATH = "/persons";

    @Test
    @DisplayName("Should create, read, update and delete a person")
    void crudOperations() throws Exception {
        // Create a person
        Person personToCreate = new Person(null, "John", "Doe", 30);
        MvcResult createResult = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andReturn();

        // Extract the created person's ID
        Person createdPerson = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Person.class);
        Long personId = createdPerson.getId();
        assertNotNull(personId);

        // Read the person
        mockMvc.perform(get(API_PATH + "/{id}", personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(personId.intValue())))
                .andExpect(jsonPath("$.firstName", is("John")));

        // Update the person
        Person personToUpdate = new Person(personId, "John", "Updated", 31);
        mockMvc.perform(put(API_PATH + "/{id}", personId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("Updated")))
                .andExpect(jsonPath("$.age", is(31)));

        // Delete the person
        mockMvc.perform(delete(API_PATH + "/{id}", personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify person is deleted
        mockMvc.perform(get(API_PATH + "/{id}", personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all persons")
    void findAll() throws Exception {
        // Create test persons
        createTestPerson("John", "Doe", 30);
        createTestPerson("Jane", "Smith", 25);

        // Get all persons
        mockMvc.perform(get(API_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should handle validation errors")
    void validationErrors() throws Exception {
        // Person with invalid age
        Person invalidPerson = new Person(null, "John", "Doe", -5);
        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest());

        // Person with empty name
        Person emptyNamePerson = new Person(null, "", "Doe", 30);
        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyNamePerson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should find persons by age range")
    void findByAgeRange() throws Exception {
        // Create test persons with different ages
        createTestPerson("Young", "Person", 20);
        createTestPerson("Middle", "Person", 30);
        createTestPerson("Old", "Person", 40);

        // Find by age range
        mockMvc.perform(get(API_PATH + "/age-range")
                .param("min", "25")
                .param("max", "35")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].firstName", hasItem("Middle")))
                .andExpect(jsonPath("$[*].firstName", not(hasItem("Young"))))
                .andExpect(jsonPath("$[*].firstName", not(hasItem("Old"))));
    }

    @Test
    @DisplayName("Should find persons by last name")
    void findByLastName() throws Exception {
        // Create test persons with different last names
        createTestPerson("John", "Smith", 30);
        createTestPerson("Jane", "Smith", 25);
        createTestPerson("Bob", "Johnson", 40);

        // Find by last name
        mockMvc.perform(get(API_PATH + "/by-last-name")
                .param("lastName", "Smith")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].firstName", hasItems("John", "Jane")));
    }

    private Person createTestPerson(String firstName, String lastName, int age) throws Exception {
        Person person = new Person(null, firstName, lastName, age);
        MvcResult result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Person.class);
    }
}
