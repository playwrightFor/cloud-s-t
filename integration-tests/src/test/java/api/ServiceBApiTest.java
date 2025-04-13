package api;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

//public class ServiceBApiTest extends TestRunner {
//
//    @Test
//    void testHelloEndpoint() {
//        APIResponse response = ApiClient.get(TestConfig.getServiceBEndpoint());
//
//        assertEquals(200, response.status(), "Статус код должен быть 200 OK");
//        assertEquals(
//                "Приветствую! Вы в приложении: App-2",
//                response.text(),
//                "Тело ответа должно соответствовать ожидаемому"
//        );
//    }
//}

@Epic("ServiceB Тестирование")
@Feature("Основные функции ServiceB")
@Story("Проверка базовых эндпоинтов сервиса")
public class ServiceBApiTest extends TestRunner {
    private static final String HELLO_ENDPOINT = TestConfig.getServiceBEndpoint();
    private static final String EXPECTED_RESPONSE_TEXT = "Приветствую! Вы в приложении: App-2";

    @Test
    @DisplayName("Успешный ответ от /hello эндпоинта")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("Smoke")
    @Owner("API Team")
    void shouldReturnValidGreeting_whenHelloEndpointCalled() {
        final APIResponse response = executeApiCall(HELLO_ENDPOINT);

        assertAll("Проверка ответа /hello",
                () -> assertStatusCode(response, 200),
                () -> assertResponseTextEquals(response, EXPECTED_RESPONSE_TEXT),
                () -> assertResponseTimeWithinLimit(response)
        );
    }

    //$ ===>>> region Вспомогательные методы
    private APIResponse executeApiCall(String endpoint) {
        return Allure.step("Выполнение запроса к " + endpoint, () -> {
            Allure.addAttachment("Request", "text/plain", "GET " + endpoint);
            final APIResponse response = ApiClient.get(endpoint);
            logResponseDetails(response);
            return response;
        });
    }

    private void logResponseDetails(APIResponse response) {
        Allure.addAttachment("Response", "application/json", response.text());
        Allure.addAttachment("Headers", "text/plain", formatHeaders(response.headers()));
        logger.debug("Response details: {}", response);
    }

    //$ ===>>> region Утилитарные assertions (можно вынести в базовый класс)
    private void assertStatusCode(APIResponse response, int expected) {
        assertEquals(expected, response.status(), "Неверный статус код");
    }

    private void assertResponseTextEquals(APIResponse response, String expected) {
        assertEquals(expected, response.text(), "Текст ответа не соответствует ожидаемому");
    }

    private void assertResponseTimeWithinLimit(APIResponse response) {
        long responseTime = response.headers().containsKey("x-response-time")
                ? Long.parseLong(response.headers().get("x-response-time"))
                : 0;

        assertTrue(responseTime <= TestConfig.getRequestTimeout(),
                "Время ответа превышает лимит: " + responseTime + "ms");
    }

    private String formatHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }
}