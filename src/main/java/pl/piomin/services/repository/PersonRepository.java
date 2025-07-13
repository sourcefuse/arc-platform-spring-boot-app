package pl.piomin.services.repository;

import org.springframework.stereotype.Repository;
import pl.piomin.services.domain.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PersonRepository {

    private final ConcurrentHashMap<Long, Person> persons = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public Person save(Person person) {
        if (person.getId() == null) {
            // New person
            person.setId(idCounter.incrementAndGet());
        }
        persons.put(person.getId(), person);
        return person;
    }

    public List<Person> findAll() {
        return new ArrayList<>(persons.values());
    }

    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(persons.get(id));
    }

    public void deleteById(Long id) {
        persons.remove(id);
    }
}
