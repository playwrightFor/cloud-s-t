package api;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

@Feature("Маршрутизация запросов")
@Story("Проверка корректной маршрутизации через Gateway")
public class GatewayApiTest extends TestRunner {

    @ParameterizedTest(name = "[{index}] Проверка эндпоинта {0}")
    @CsvSource({
            "/serviceA/hello, Приветствую! Вы в приложении: App-1",
            "/serviceB/hello, Приветствую! Вы в приложении: App-2"
    })
    @DisplayName("Проверка маршрутизации Gateway")
    @Severity(SeverityLevel.CRITICAL)
    void testGatewayRoutes(String endpoint, String expectedResponse) {
        Allure.step("Выполнение запроса к " + endpoint, () -> {
            APIResponse response = ApiClient.get(endpoint);

            Allure.addAttachment("Ответ", "application/json", response.text());

            assertAll(
                    () -> assertEquals(200, response.status(),
                            "Неверный статус код для эндпоинта " + endpoint),
                    () -> assertEquals(expectedResponse, response.text(),
                            "Текст ответа не соответствует ожидаемому"),
                    () -> assertTrue(response.url().contains(endpoint),
                            "URL ответа содержит неверный путь")
            );
        });
    }

    @Test
    @DisplayName("Проверка обработки несуществующих путей")
    @Severity(SeverityLevel.NORMAL)
    @Owner("API Team")
    void testGatewayReturns404ForInvalidPath() {
        Allure.parameter("URL", "/non-existing-service/hello");

        APIResponse response = ApiClient.get("/non-existing-service/hello");

        assertAll(
                () -> assertEquals(404, response.status()),
                () -> assertTrue(response.text().contains("Not Found"))
        );
    }

    @Test
    @DisplayName("Проверка балансировки нагрузки")
    @Tag("LoadTest")
    void testGatewayLoadBalancing() {
        Allure.step("Первый запрос", () -> {
            APIResponse response1 = ApiClient.get("/serviceA/hello");
            assertEquals(200, response1.status());
        });

        Allure.step("Второй запрос", () -> {
            APIResponse response2 = ApiClient.get("/serviceA/hello");
            assertEquals(200, response2.status());
        });

        Allure.step("Сравнение ответов", () -> {
            APIResponse response1 = ApiClient.get("/serviceA/hello");
            APIResponse response2 = ApiClient.get("/serviceA/hello");
            assertEquals(response1.text(), response2.text());
        });
    }

//        @Test
//    void testEnvironmentConfiguration() {
//        assertAll(
//                () -> assertFalse(TestConfig.getEnvironment().isEmpty()),
//                () -> assertTrue(TestConfig.getGatewayUrl().startsWith("http"))
//        );
//    }


//    @Test
//    @DisplayName("Проверка конфигурации окружения") // Человеко-читаемое название
//    @Severity(SeverityLevel.BLOCKER) // Уровень критичности
//    @Tag("ConfigCheck") // Группировка тестов
//    @Tag("Smoke")       // Дополнительная категоризация тестов (при необходимости)
//    void testEnvironmentConfiguration() {
//        Allure.description("Тест проверяет корректность базовых настроек окружения");
//
//        Allure.step("Проверка настроек окружения", () -> {
//            String environment = TestConfig.getEnvironment();
//
//            assertAll(
//                    () -> assertFalse(environment.isEmpty(),
//                            "Окружение не должно быть пустым. Актуальное значение: " + environment),
//                    () -> assertTrue(environment.matches("local|staging|prod"),
//                            "Недопустимое окружение: " + environment)
//            );
//        });
//
//        Allure.step("Проверка URL шлюза", () -> {
//            String gatewayUrl = TestConfig.getGatewayUrl();
//
//            assertAll(
//                    () -> assertNotNull(gatewayUrl, "URL шлюза должен быть задан"),
//                    () -> assertTrue(gatewayUrl.startsWith("http"),
//                            "URL должен содержать протокол. Актуальный URL: " + gatewayUrl),
//                    () -> assertTrue(gatewayUrl.matches("^http(s)?://.*:[0-9]{4}"),
//                            "Некорректный формат URL: " + gatewayUrl)
//            );
//        });
//    }
}