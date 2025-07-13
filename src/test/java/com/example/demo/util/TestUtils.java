package com.example.demo.util;

import com.example.demo.model.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating test data and helper methods for tests.
 */
public class TestUtils {

    /**
     * Creates a sample Person object for testing.
     *
     * @param id The ID to set (can be null for new entities)
     * @return A Person object with sample data
     */
    public static Person createSamplePerson(Long id) {
        return new Person(id, "John", "Doe", 30);
    }

    /**
     * Creates a list of sample Person objects for testing.
     *
     * @param count The number of Person objects to create
     * @return A list of Person objects with sample data
     */
    public static List<Person> createSamplePersonList(int count) {
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            persons.add(new Person((long) (i + 1), "FirstName" + i, "LastName" + i, 20 + i));
        }
        return persons;
    }
}
