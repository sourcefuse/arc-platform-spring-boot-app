import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ServiceTest {

    @MockBean
    private ExternalService externalService;

    @Autowired
    private MyService myService;

    @Test
    void shouldHandleBusinessLogic() {
        // Arrange
        when(externalService.call()).thenReturn("expectedResponse");

        // Act
        String result = myService.process();

        // Assert
        assertEquals("expectedResponse", result);
        verify(externalService, times(1)).call();
    }

    @Test
    void shouldHandleExceptionScenario() {
        // Arrange
        when(externalService.call()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> myService.process());
    }
}