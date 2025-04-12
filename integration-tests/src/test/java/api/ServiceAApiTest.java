package api;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceAApiTest {

    @BeforeAll
    static void setUp() {
        ApiClient.setup(TestConfig.getGatewayUrl());
    }

    @Test
    void testHelloEndpoint() {
        APIResponse response = ApiClient.get("/serviceA/hello");

        assertEquals(200, response.status(), "Статус код должен быть 200");
        assertEquals(
                "Приветствую! Вы в приложении: App-1",
                response.text(),
                "Тело ответа должно соответствовать ожидаемому"
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/serviceA/hello, 200",
            "/serviceA/invalid, 404"
    })
    void testDifferentEndpoints(String endpoint, int expectedStatus) {
        APIResponse response = ApiClient.get(endpoint);
        assertEquals(expectedStatus, response.status());
    }

    @Test
    void testResponseHeaders() {
        APIResponse response = ApiClient.get("/serviceA/hello");

        assertAll(
                () -> assertEquals(200, response.status()),
                () -> assertEquals(
                        "text/plain;charset=UTF-8",
                        response.headers().get("content-type"),
                        "Content-Type должен быть text/plain"
                ),
                () -> assertNotNull(
                        response.headers().get("date"),
                        "Заголовок Date должен присутствовать"
                )
        );
    }

    @AfterAll
    static void tearDown() {
        ApiClient.teardown();
    }
}