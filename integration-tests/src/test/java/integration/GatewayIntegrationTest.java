package integration;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

//public class GatewayIntegrationTest extends TestRunner {
//
//    @BeforeAll
//    static void setUp() {
//        ApiClient.setup(TestConfig.getGatewayUrl());
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//            "/serviceA/hello, Приветствую! Вы в приложении: App-1",
//            "/serviceB/hello, Приветствую! Вы в приложении: App-2"
//    })
//    void testRouteToCorrectService(String endpoint, String expectedResponse) {
//        APIResponse response = ApiClient.get(endpoint);
//
//        assertAll(
//                () -> assertEquals(200, response.status(),
//                        "Неверный статус код для эндпоинта " + endpoint),
//                () -> assertEquals(expectedResponse, response.text(),
//                        "Несоответствие тела ответа для эндпоинта " + endpoint),
//                () -> assertTrue(response.url().contains(endpoint),
//                        "URL ответа должен содержать путь " + endpoint)
//        );
//    }
//
//    @Test
//    void testInvalidRouteReturns404() {
//        APIResponse response = ApiClient.get("/invalid-endpoint");
//
//        assertEquals(404, response.status(),
//                "Несуществующий эндпоинт должен возвращать 404. Актуальный статус: " + response.status());
//    }
//}

@Epic("Интеграционное тестирование Gateway")
@Feature("Маршрутизация запросов")
@Story("Проверка корректной работы Gateway")
public class GatewayIntegrationTest extends TestRunner {
    private static final String INVALID_ENDPOINT = "/invalid-endpoint";
    private static final String SERVICE_A_HELLO = TestConfig.getServiceAEndpoint();
    private static final String SERVICE_B_HELLO = TestConfig.getServiceBEndpoint();

    @BeforeAll
    static void setupApiClient() {
        ApiClient.setup(TestConfig.getGatewayUrl());
        Allure.addAttachment("Gateway URL", "text/plain", TestConfig.getGatewayUrl());
    }

    @ParameterizedTest(name = "Маршрут {0} → ответ сервиса")
    @MethodSource("provideServiceEndpoints")
    @DisplayName("Проверка корректной маршрутизации запросов")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("Integration")
    void shouldRouteToCorrectService_whenValidEndpointProvided(String endpoint, String expectedResponse) {
        APIResponse response = executeApiCall(endpoint);

        assertAll("Проверка ответа для эндпоинта: " + endpoint,
                () -> assertStatusCode(response, 200),
                () -> assertResponseTextEquals(response, expectedResponse),
                () -> assertUrlContainsPath(response, endpoint)
        );
    }

    @Test
    @DisplayName("Проверка обработки несуществующего маршрута")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("ErrorHandling")
    void shouldReturn404_whenInvalidRouteRequested() {
        APIResponse response = executeApiCall(INVALID_ENDPOINT);

        assertAll("Проверка ответа для несуществующего пути",
                () -> assertStatusCode(response, 404),
                () -> assertResponseContains(response, "Not Found")
        );
    }

    //$ ===>>> region Вспомогательные методы
    private static Stream<Arguments> provideServiceEndpoints() {
        return Stream.of(
                Arguments.of(SERVICE_A_HELLO, "Приветствую! Вы в приложении: App-1"),
                Arguments.of(SERVICE_B_HELLO, "Приветствую! Вы в приложении: App-2")
        );
    }

    private APIResponse executeApiCall(String endpoint) {
        return Allure.step("Выполнение запроса к " + endpoint, () -> {
            Allure.addAttachment("Request", "text/plain", "GET " + endpoint);
            APIResponse response = ApiClient.get(endpoint);
            logResponseDetails(response);
            return response;
        });
    }

    private void logResponseDetails(APIResponse response) {
        Allure.addAttachment("Response", "application/json", response.text());
        logger.debug("Response details - Status: {}, Body: {}", response.status(), response.text());
    }
    // endregion

    //$ ===>>> region Утилитарные assertions
    private void assertStatusCode(APIResponse response, int expected) {
        assertEquals(expected, response.status(), "Неверный статус код");
    }

    private void assertResponseTextEquals(APIResponse response, String expected) {
        assertEquals(expected, response.text(), "Текст ответа не соответствует ожидаемому");
    }

    private void assertUrlContainsPath(APIResponse response, String path) {
        assertTrue(response.url().contains(path),
                "URL ответа должен содержать путь: " + path);
    }

    private void assertResponseContains(APIResponse response, String expectedText) {
        assertTrue(response.text().contains(expectedText),
                "Ответ должен содержать текст: " + expectedText);
    }
}