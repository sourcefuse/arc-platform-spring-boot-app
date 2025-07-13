package pl.piomin.services.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

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

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    public Optional<Person> update(Long id, Person person) {
        Optional<Person> existingPerson = personRepository.findById(id);
        if (existingPerson.isPresent()) {
            // Update fields from the input person
            Person personToUpdate = existingPerson.get();
            if (person.getFirstName() != null) {
                personToUpdate.setFirstName(person.getFirstName());
            }
            if (person.getLastName() != null) {
                personToUpdate.setLastName(person.getLastName());
            }
            // Add other fields as needed
            
            return Optional.of(personRepository.save(personToUpdate));
        }
        return Optional.empty();
    }

    public void delete(Long id) {
        personRepository.deleteById(id);
    }
}
