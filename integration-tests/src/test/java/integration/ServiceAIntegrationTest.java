package integration;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

//public class ServiceAIntegrationTest extends TestRunner {
//
//    @Test
//    void testRouting() {
//        APIResponse response = ApiClient.get("/serviceA/hello");
//        assertEquals(200, response.status());
//    }
//}

@Epic("ServiceA Тестирование")
@Feature("Интеграционные тесты")
@Story("Проверка базовой функциональности ServiceA")
public class ServiceAIntegrationTest extends TestRunner {
    private static final String SERVICE_A_HELLO_ENDPOINT = TestConfig.getServiceAEndpoint();

    @Test
    @DisplayName("Проверка доступности основного эндпоинта")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("Smoke")
    void shouldReturnSuccessStatus_whenValidEndpointCalled() {
        APIResponse response = executeApiCall();
        assertStatusCode(response);
    }

    //$ ===>>> region Вспомогательные методы
    private APIResponse executeApiCall() {
        return Allure.step("Выполнение запроса к " + ServiceAIntegrationTest.SERVICE_A_HELLO_ENDPOINT, () -> {
            Allure.addAttachment("Request", "text/plain", "GET " + ServiceAIntegrationTest.SERVICE_A_HELLO_ENDPOINT);
            APIResponse response = ApiClient.get(ServiceAIntegrationTest.SERVICE_A_HELLO_ENDPOINT);
            logResponseDetails(response);
            return response;
        });
    }

    private void logResponseDetails(APIResponse response) {
        Allure.addAttachment("Response", "application/json", response.text());
        logger.debug("ServiceA Response - Status: {}, Body: {}",
                response.status(), response.text());
    }

    private void assertStatusCode(APIResponse response) {
        assertEquals(200, response.status(),
                "Неверный статус код для эндпоинта " + SERVICE_A_HELLO_ENDPOINT);
    }
}