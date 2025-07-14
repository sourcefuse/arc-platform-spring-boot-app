package pl.piomin.services.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.piomin.services.domain.Person;
import pl.piomin.services.service.PersonService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Person createPerson(@Valid @RequestBody Person person) {
        return personService.add(person);
    }

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable Long id) {
        return personService.findById(id);
    }

    @GetMapping
    public List<Person> getAllPersons() {
        return personService.findAll();
    }

    @PutMapping("/{id}")
    public Person updatePerson(@PathVariable Long id, @Valid @RequestBody Person person) {
        person.setId(id);
        return personService.update(person);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerson(@PathVariable Long id) {
        personService.delete(id);
    }

    @GetMapping("/search")
    public List<Person> findPersonsByFirstName(@RequestParam String firstName) {
        return personService.findByFirstName(firstName);
    }
}