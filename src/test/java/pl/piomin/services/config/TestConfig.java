package pl.piomin.services.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pl.piomin.services.repository.PersonRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Creates a mock PersonRepository bean for unit testing.
     * This bean is only active when the "test" profile is active.
     *
     * @return A mock PersonRepository
     */
    @Bean
    @Primary
    public PersonRepository mockPersonRepository() {
        return mock(PersonRepository.class);
    }
}
