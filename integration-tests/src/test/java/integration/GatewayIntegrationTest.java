package integration;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

public class GatewayIntegrationTest {

    @BeforeAll
    static void setUp() {
        ApiClient.setup(TestConfig.getGatewayUrl());
    }

    @ParameterizedTest
    @CsvSource({
            "/serviceA/hello, Приветствую! Вы в приложении: App-1",
            "/serviceB/hello, Приветствую! Вы в приложении: App-2"
    })
    void testRouteToCorrectService(String endpoint, String expectedResponse) {
        APIResponse response = ApiClient.get(endpoint);

        assertAll(
                () -> assertEquals(200, response.status(),
                        "Неверный статус код для эндпоинта " + endpoint),
                () -> assertEquals(expectedResponse, response.text(),
                        "Несоответствие тела ответа для эндпоинта " + endpoint),
                () -> assertTrue(response.url().contains(endpoint),
                        "URL ответа должен содержать путь " + endpoint)
        );
    }

    @Test
    void testInvalidRouteReturns404() {
        APIResponse response = ApiClient.get("/invalid-endpoint");

        assertEquals(404, response.status(),
                "Несуществующий эндпоинт должен возвращать 404. Актуальный статус: " + response.status());
    }

    @AfterAll
    static void tearDown() {
        ApiClient.teardown();
    }
}