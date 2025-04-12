package api;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceBApiTest extends TestRunner {

    @Test
    void testHelloEndpoint() {
        APIResponse response = ApiClient.get(TestConfig.getServiceBEndpoint());

        assertEquals(200, response.status(), "Статус код должен быть 200 OK");
        assertEquals(
                "Приветствую! Вы в приложении: App-2",
                response.text(),
                "Тело ответа должно соответствовать ожидаемому"
        );
    }
}