package com.example.demo.controller;

import com.example.demo.model.Person;
import com.example.demo.service.PersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_PATH = "/persons";

    @Test
    @DisplayName("Should return all persons")
    void findAll() throws Exception {
        // Given
        Person person1 = new Person(1L, "John", "Doe", 30);
        Person person2 = new Person(2L, "Jane", "Smith", 25);
        List<Person> persons = Arrays.asList(person1, person2);
        
        when(personService.findAll()).thenReturn(persons);

        // When & Then
        mockMvc.perform(get(API_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstName", is("Jane")));

        verify(personService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no persons exist")
    void findAllEmpty() throws Exception {
        // Given
        when(personService.findAll()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get(API_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(personService).findAll();
    }

    @Test
    @DisplayName("Should return person by ID when exists")
    void findById() throws Exception {
        // Given
        Person person = new Person(1L, "John", "Doe", 30);
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        // When & Then
        mockMvc.perform(get(API_PATH + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.age", is(30)));

        verify(personService).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when person not found")
    void findByIdNotFound() throws Exception {
        // Given
        when(personService.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get(API_PATH + "/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(personService).findById(999L);
    }

    @Test
    @DisplayName("Should create a new person")
    void add() throws Exception {
        // Given
        Person personToCreate = new Person(null, "John", "Doe", 30);
        Person createdPerson = new Person(1L, "John", "Doe", 30);
        
        when(personService.add(any(Person.class))).thenReturn(createdPerson);

        // When & Then
        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(personService).add(any(Person.class));
    }

    @Test
    @DisplayName("Should return 400 when creating person with invalid data")
    void addInvalidPerson() throws Exception {
        // Given
        Person invalidPerson = new Person(null, "", "", -5); // Invalid data

        // When & Then
        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest());

        verify(personService, never()).add(any(Person.class));
    }

    @Test
    @DisplayName("Should update an existing person")
    void update() throws Exception {
        // Given
        Person personToUpdate = new Person(1L, "John", "Updated", 31);
        when(personService.update(any(Person.class))).thenReturn(personToUpdate);
        when(personService.findById(1L)).thenReturn(Optional.of(new Person(1L, "John", "Doe", 30)));

        // When & Then
        mockMvc.perform(put(API_PATH + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.lastName", is("Updated")));

        verify(personService).update(any(Person.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent person")
    void updateNonExistent() throws Exception {
        // Given
        Person personToUpdate = new Person(999L, "John", "Updated", 31);
        when(personService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put(API_PATH + "/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personToUpdate)))
                .andExpect(status().isNotFound());

        verify(personService, never()).update(any(Person.class));
    }

    @Test
    @DisplayName("Should delete a person successfully")
    void delete() throws Exception {
        // Given
        when(personService.findById(1L)).thenReturn(Optional.of(new Person(1L, "John", "Doe", 30)));
        doNothing().when(personService).delete(1L);

        // When & Then
        mockMvc.perform(delete(API_PATH + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(personService).delete(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent person")
    void deleteNonExistent() throws Exception {
        // Given
        when(personService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete(API_PATH + "/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(personService, never()).delete(999L);
    }
}
