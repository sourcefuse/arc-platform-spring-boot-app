package pl.piomin.services.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person add(Person person) {
        return personRepository.save(person);
    }

    public Person findById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Person update(Person person) {
        // Verify person exists
        findById(person.getId());
        return personRepository.save(person);
    }

    public void delete(Long id) {
        // Verify person exists
        findById(id);
        personRepository.deleteById(id);
    }

    public List<Person> findByFirstName(String firstName) {
        return personRepository.findByFirstName(firstName);
    }
}