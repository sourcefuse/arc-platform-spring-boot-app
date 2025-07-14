package pl.piomin.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.piomin.services.domain.Person;
import pl.piomin.services.service.PersonNotFoundException;
import pl.piomin.services.service.PersonService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
public class PersonControllerMockMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create person and return 201 status")
    void shouldCreatePerson() throws Exception {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(null);
        
        Person savedPerson = Instancio.create(Person.class);
        savedPerson.setId(1L);
        savedPerson.setFirstName(person.getFirstName());
        savedPerson.setLastName(person.getLastName());
        
        when(personService.add(any(Person.class))).thenReturn(savedPerson);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is(savedPerson.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(savedPerson.getLastName())));
        
        verify(personService, times(1)).add(any(Person.class));
    }

    @Test
    @DisplayName("Should get person by ID")
    void shouldGetPersonById() throws Exception {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(1L);
        
        when(personService.findById(1L)).thenReturn(person);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is(person.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(person.getLastName())));
        
        verify(personService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when person not found")
    void shouldReturn404WhenPersonNotFound() throws Exception {
        // Arrange
        when(personService.findById(anyLong())).thenThrow(new PersonNotFoundException("Person not found with id: 1"));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/1"))
                .andExpect(status().isNotFound());
        
        verify(personService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should get all persons")
    void shouldGetAllPersons() throws Exception {
        // Arrange
        Person person1 = Instancio.create(Person.class);
        person1.setId(1L);
        
        Person person2 = Instancio.create(Person.class);
        person2.setId(2L);
        
        List<Person> persons = Arrays.asList(person1, person2);
        
        when(personService.findAll()).thenReturn(persons);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
        
        verify(personService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update person")
    void shouldUpdatePerson() throws Exception {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(1L);
        
        Person updatedPerson = Instancio.create(Person.class);
        updatedPerson.setId(1L);
        updatedPerson.setFirstName("UpdatedFirstName");
        
        when(personService.update(any(Person.class))).thenReturn(updatedPerson);
        
        // Act & Assert
        mockMvc.perform(put("/api/v1/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("UpdatedFirstName")));
        
        verify(personService, times(1)).update(any(Person.class));
    }

    @Test
    @DisplayName("Should delete person")
    void shouldDeletePerson() throws Exception {
        // Arrange
        doNothing().when(personService).delete(anyLong());
        
        // Act & Assert
        mockMvc.perform(delete("/api/v1/1"))
                .andExpect(status().isNoContent());
        
        verify(personService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Should find persons by first name")
    void shouldFindPersonsByFirstName() throws Exception {
        // Arrange
        Person person = Instancio.create(Person.class);
        person.setId(1L);
        person.setFirstName("John");
        
        List<Person> persons = Arrays.asList(person);
        
        when(personService.findByFirstName("John")).thenReturn(persons);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/search?firstName=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")));
        
        verify(personService, times(1)).findByFirstName("John");
    }

    @Test
    @DisplayName("Should return 400 when creating person with invalid data")
    void shouldReturn400WhenCreatingPersonWithInvalidData() throws Exception {
        // Arrange
        Person invalidPerson = new Person();
        // Leave required fields empty
        
        // Act & Assert
        mockMvc.perform(post("/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest());
        
        verify(personService, never()).add(any(Person.class));
    }
}