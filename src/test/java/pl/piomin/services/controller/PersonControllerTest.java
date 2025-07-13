package pl.piomin.services.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.piomin.services.domain.Person;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PersonControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PersonController personController;

    @MockBean
    private List<Person> personList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(personController).build();
    }

    @Test
    void shouldReturnAllPersons() throws Exception {
        when(personList.size()).thenReturn(2);
        when(personList.get(0)).thenReturn(new Person(1L));
        when(personList.get(1)).thenReturn(new Person(2L));

        mockMvc.perform(get("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldAddPerson() throws Exception {
        Person person = new Person();
        person.setId(1L);

        mockMvc.perform(post("/api/v1")
                .contentType("application/json")
                .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Additional tests for findById, update, and delete
}
