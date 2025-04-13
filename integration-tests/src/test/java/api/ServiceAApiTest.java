package api;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

//public class ServiceAApiTest extends TestRunner {
//
//
//    @Test
//    void testHelloEndpoint() {
//        APIResponse response = ApiClient.get(TestConfig.getServiceAEndpoint());
//
//        assertEquals(200, response.status(), "Статус код должен быть 200");
//        assertEquals(
//                "Приветствую! Вы в приложении: App-1",
//                response.text(),
//                "Тело ответа должно соответствовать ожидаемому"
//        );
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//            "/serviceA/hello, 200",
//            "/serviceA/invalid, 404"
//    })
//    void testDifferentEndpoints(String endpoint, int expectedStatus) {
//        APIResponse response = ApiClient.get(endpoint);
//        assertEquals(expectedStatus, response.status());
//    }
//
//    @Test
//    void testResponseHeaders() {
//        APIResponse response = ApiClient.get("/serviceA/hello");
//
//        assertAll(
//                () -> assertEquals(200, response.status()),
//                () -> assertEquals(
//                        "text/plain;charset=UTF-8",
//                        response.headers().get("content-type"),
//                        "Content-Type должен быть text/plain"
//                ),
//                () -> assertNotNull(
//                        response.headers().get("date"),
//                        "Заголовок Date должен присутствовать"
//                )
//        );
//    }
//}

@Epic("ServiceA Тестирование")
@Feature("Основные функции ServiceA")
@Story("Проверка базовых эндпоинтов сервиса")
public class ServiceAApiTest extends TestRunner {
    private static final String HELLO_ENDPOINT = TestConfig.getServiceAEndpoint();
    private static final String INVALID_ENDPOINT = "/serviceA/invalid";
    private static final String EXPECTED_CONTENT_TYPE = "text/plain;charset=UTF-8";
    private static final String EXPECTED_RESPONSE_TEXT = "Приветствую! Вы в приложении: App-1";

    @Test
    @DisplayName("Успешный ответ от /hello эндпоинта")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("Smoke")
    void shouldReturnValidGreeting_whenHelloEndpointCalled() {
        final APIResponse response = executeApiCall(HELLO_ENDPOINT);

        assertAll("Проверка ответа /hello",
                () -> assertStatusCode(response, 200),
                () -> assertResponseTextEquals(response, EXPECTED_RESPONSE_TEXT)
        );
    }

    @ParameterizedTest(name = "Проверка эндпоинта {0} → ожидаемый статус: {1}")
    @MethodSource("provideEndpointsAndStatuses")
    @DisplayName("Валидация доступности эндпоинтов")
    @Severity(SeverityLevel.NORMAL)
    @Tag("Regression")
    void shouldReturnCorrectStatus_whenDifferentEndpointsCalled(String endpoint, int expectedStatus) {
        final APIResponse response = executeApiCall(endpoint);
        assertStatusCode(response, expectedStatus);
    }

    @Test
    @DisplayName("Проверка корректности заголовков ответа")
    @Severity(SeverityLevel.MINOR)
    @Tag("HeadersValidation")
    void shouldContainRequiredHeaders_whenRequestProcessed() {
        final APIResponse response = executeApiCall(HELLO_ENDPOINT);

        assertAll("Проверка заголовков ответа",
                () -> assertStatusCode(response, 200),
                () -> assertContentType(response, EXPECTED_CONTENT_TYPE),
                () -> assertHeaderExists(response, "date")
        );
    }

    //$ ===>>> region Вспомогательные методы
    private static Stream<Arguments> provideEndpointsAndStatuses() {
        return Stream.of(
                Arguments.of(HELLO_ENDPOINT, 200),
                Arguments.of(INVALID_ENDPOINT, 404)
        );
    }

    private APIResponse executeApiCall(String endpoint) {
        return Allure.step("Выполнение запроса к " + endpoint, () -> {
            Allure.addAttachment("Request", "text/plain", "GET " + endpoint);
            final APIResponse response = ApiClient.get(endpoint);
            logResponseDetails(response);
            return response;
        });
    }

    private void logResponseDetails(APIResponse response) {
        Allure.addAttachment("Response Headers", "text/plain", formatHeaders(response.headers()));
        logger.debug("Response details: {}", response);
    }

    //$ ===>>> region Утилитарные assertions
    private void assertStatusCode(APIResponse response, int expected) {
        assertEquals(expected, response.status(), "Неверный статус код");
    }

    private void assertResponseTextEquals(APIResponse response, String expected) {
        assertEquals(expected, response.text(), "Текст ответа не соответствует ожидаемому");
    }

    private void assertContentType(APIResponse response, String expectedType) {
        assertEquals(expectedType, response.headers().get("content-type"),
                "Content-Type заголовок не соответствует ожидаемому");
    }

    private void assertHeaderExists(APIResponse response, String headerName) {
        assertNotNull(response.headers().get(headerName),
                "Заголовок " + headerName + " должен присутствовать");
    }

    private String formatHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }
}