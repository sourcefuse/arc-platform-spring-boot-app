package pl.piomin.services.person;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1/persons")
public class PersonServiceApplication {

    private final List<Person> persons = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(PersonServiceApplication.class, args);
    }

    @GetMapping
    public List<Person> findAll() {
        return persons;
    }

    @GetMapping("/{id}")
    public Person findById(@PathVariable Long id) {
        return persons.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @PostMapping
    public Person add(@RequestBody Person person) {
        person.setId((long) (persons.size() + 1));
        persons.add(person);
        return person;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        persons.removeIf(p -> p.getId().equals(id));
    }
}