package api;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceBApiTest {

    @BeforeAll
    static void setUp() {
        ApiClient.setup(TestConfig.getGatewayUrl());
    }

    @Test
    void testHelloEndpoint() {
        APIResponse response = ApiClient.get("/serviceB/hello");

        assertEquals(200, response.status(), "Статус код должен быть 200 OK");
        assertEquals(
                "Приветствую! Вы в приложении: App-2",
                response.text(),
                "Тело ответа должно соответствовать ожидаемому"
        );
    }

    @AfterAll
    static void tearDown() {
        ApiClient.teardown();
    }
}