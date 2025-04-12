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

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

//@Feature("Маршрутизация запросов")
//@Story("Проверка корректной маршрутизации через Gateway")
//public class GatewayApiTest extends TestRunner {
//
//    @ParameterizedTest(name = "[{index}] Проверка эндпоинта {0}")
//    @CsvSource({
//            "/serviceA/hello, Приветствую! Вы в приложении: App-1",
//            "/serviceB/hello, Приветствую! Вы в приложении: App-2"
//    })
//    @DisplayName("Проверка маршрутизации Gateway")
//    @Severity(SeverityLevel.CRITICAL)
//    void testGatewayRoutes(String endpoint, String expectedResponse) {
//        Allure.step("Выполнение запроса к " + endpoint, () -> {
//            APIResponse response = ApiClient.get(endpoint);
//
//            Allure.addAttachment("Ответ", "application/json", response.text());
//
//            assertAll(
//                    () -> assertEquals(200, response.status(),
//                            "Неверный статус код для эндпоинта " + endpoint),
//                    () -> assertEquals(expectedResponse, response.text(),
//                            "Текст ответа не соответствует ожидаемому"),
//                    () -> assertTrue(response.url().contains(endpoint),
//                            "URL ответа содержит неверный путь")
//            );
//        });
//    }
//
//    @Test
//    @DisplayName("Проверка обработки несуществующих путей")
//    @Severity(SeverityLevel.NORMAL)
//    @Owner("API Team")
//    void testGatewayReturns404ForInvalidPath() {
//        Allure.parameter("URL", "/non-existing-service/hello");
//
//        APIResponse response = ApiClient.get("/non-existing-service/hello");
//
//        assertAll(
//                () -> assertEquals(404, response.status()),
//                () -> assertTrue(response.text().contains("Not Found"))
//        );
//    }
//
//    @Test
//    @DisplayName("Проверка балансировки нагрузки")
//    @Tag("LoadTest")
//    void testGatewayLoadBalancing() {
//        Allure.step("Первый запрос", () -> {
//            APIResponse response1 = ApiClient.get("/serviceA/hello");
//            assertEquals(200, response1.status());
//        });
//
//        Allure.step("Второй запрос", () -> {
//            APIResponse response2 = ApiClient.get("/serviceA/hello");
//            assertEquals(200, response2.status());
//        });
//
//        Allure.step("Сравнение ответов", () -> {
//            APIResponse response1 = ApiClient.get("/serviceA/hello");
//            APIResponse response2 = ApiClient.get("/serviceA/hello");
//            assertEquals(response1.text(), response2.text());
//        });
//    }
//}

@Epic("API Gateway Тестирование")
@Feature("Маршрутизация запросов")
@Story("Проверка корректной маршрутизации через Gateway")
public class GatewayApiTest extends TestRunner {
    private static final String INVALID_PATH = "/non-existing-service/hello";
    private static final String SERVICE_A_HELLO = TestConfig.getServiceAEndpoint();
    private static final String SERVICE_B_HELLO = TestConfig.getServiceBEndpoint();

    @ParameterizedTest(name = "Проверка маршрута {0} → ожидаемый ответ: {1}")
    @MethodSource("provideValidEndpoints")
    @DisplayName("Успешная маршрутизация запросов через Gateway")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("API Team")
    @Tag("Smoke")
    void shouldRouteRequestCorrectlyWhenValidEndpointProvided(String endpoint, String expectedResponse) {
        executeApiCallAndVerify(endpoint, expectedResponse);
    }

    @Test
    @DisplayName("Получение 404 при обращении к несуществующему пути")
    @Severity(SeverityLevel.NORMAL)
    @Tag("Regression")
    void shouldReturn404WhenInvalidPathRequested() {
        final APIResponse response = executeApiCall(INVALID_PATH);

        assertAll("Проверка ответа для несуществующего пути",
                () -> assertStatusCode(response, 404),
                () -> assertResponseContains(response, "Not Found")
        );
    }

    @Test
    @DisplayName("Проверка балансировки нагрузки между инстансами сервиса")
    @Severity(SeverityLevel.MINOR)
    @Tag("LoadTest")
    @Tag("Integration")
    void shouldBalanceLoadWhenMultipleRequestsSent() {
        Allure.step("Проверка распределения запросов", () -> {

            final APIResponse firstResponse = executeApiCall(SERVICE_A_HELLO);
            final String serverHeader = firstResponse.headers().get("X-Server-ID");

            if (serverHeader != null) {
                checkServerDistribution();
            } else {
                checkBasicAvailability();
            }
        });
    }

    //$ region Вспомогательные методы
    private static Stream<Arguments> provideValidEndpoints() {
        return Stream.of(
                Arguments.of(SERVICE_A_HELLO, "Приветствую! Вы в приложении: App-1"),
                Arguments.of(SERVICE_B_HELLO, "Приветствую! Вы в приложении: App-2")
        );
    }

    private void executeApiCallAndVerify(String endpoint, String expectedResponse) {
        Allure.step("Выполнение запроса к " + endpoint, () -> {
            final APIResponse response = executeApiCall(endpoint);

            assertAll("Проверка корректности ответа для эндпоинта: " + endpoint,
                    () -> assertStatusCode(response, 200),
                    () -> assertResponseTextEquals(response, expectedResponse),
                    () -> assertUrlContainsPath(response, endpoint)
            );
        });
    }

    private APIResponse executeApiCall(String endpoint) {
        Allure.addAttachment("Request", "text/plain", "GET " + endpoint);
        final APIResponse response = ApiClient.get(endpoint);
        logResponseDetails(response);
        return response;
    }

    private void logResponseDetails(APIResponse response) {
        Allure.addAttachment("Response", "application/json", response.text());

        logger.debug("Response status: {}, body: {}", response.status(), response.text());
    }

    private void checkServerDistribution() {
        List<String> servers = IntStream.range(0, 5)
                .mapToObj(i -> executeApiCall(SERVICE_A_HELLO))
                .map(response -> response.headers().get("X-Server-ID"))
                .filter(Objects::nonNull)
                .toList();

        long uniqueServers = servers.stream().distinct().count();

        assertAll(
                () -> assertFalse(servers.isEmpty(), "Не получены заголовки серверов"),
                () -> assertTrue(uniqueServers > 1,
                        "Запросы должны распределяться между разными серверами. Найдено: " + uniqueServers)
        );
    }

    private void checkBasicAvailability() {
        IntStream.range(0, 5).forEach(i -> {
            APIResponse response = executeApiCall(SERVICE_A_HELLO);
            assertStatusCode(response, 200);
        });
        logger.warn("Проверка балансировки невозможна - отсутствует X-Server-ID");
    }

    //$ region Утилитарные assertions
    private void assertStatusCode(APIResponse response, int expected) {
        assertEquals(expected, response.status(), "Неверный статус код");
    }

    private void assertResponseTextEquals(APIResponse response, String expected) {
        assertEquals(expected, response.text(), "Текст ответа не соответствует ожидаемому");
    }

    private void assertUrlContainsPath(APIResponse response, String path) {
        assertTrue(response.url().contains(path), "URL ответа содержит неверный путь");
    }

    private void assertResponseContains(APIResponse response, String expectedText) {
        assertTrue(response.text().contains(expectedText),
                "Ответ должен содержать текст: " + expectedText);
    }
}