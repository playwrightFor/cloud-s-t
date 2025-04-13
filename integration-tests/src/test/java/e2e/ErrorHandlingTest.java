package e2e;

import com.microsoft.playwright.APIResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

//public class ErrorHandlingTest extends TestRunner {
//    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingTest.class);
//
//
//    @ParameterizedTest
//    @CsvSource({
//            "POST, /serviceA/hello",
//            "PUT, /serviceA/hello",
//            "PATCH, /serviceA/hello"
//    })
//    void testInvalidHttpMethods(String method, String endpoint) {
//        APIResponse response = switch(method) {
//            case "POST" -> ApiClient.post(endpoint);
//            case "PUT" -> ApiClient.put(endpoint);
//            case "PATCH" -> ApiClient.patch(endpoint);
//            default -> throw new IllegalArgumentException("Unsupported method: " + method);
//        };
//
//        assertEquals(405, response.status(),
//                "Method %s should return 405 for %s".formatted(method, endpoint));
//    }
//
//    @Test
//    void testMalformedRequests() {
//        APIResponse response = ApiClient.get("/serviceA/hello?invalid=param%00");
//
//        logger.debug("Malformed request response:\nStatus: {}\nBody: {}",
//                response.status(),
//                response.text()
//        );
//
//        assertEquals(400, response.status());
//    }
//}


@Epic("Обработка ошибок")
@Feature("Валидация ошибочных сценариев")
@Story("Проверка корректной обработки некорректных запросов")
public class ErrorHandlingTest extends TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingTest.class);

    @ParameterizedTest(name = "Метод {0} для {1} → 405")
    @CsvSource({
            "POST, /serviceA/hello",
            "PUT, /serviceA/hello",
            "PATCH, /serviceA/hello"
    })
    @DisplayName("Проверка обработки недопустимых HTTP-методов")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("Security")
    void testInvalidHttpMethods(String method, String endpoint) {
        APIResponse response = executeInvalidMethodCall(method, endpoint);
        assertStatusCode(response, 405);
    }

    @Test
    @DisplayName("Обработка некорректно сформированных запросов")
    @Severity(SeverityLevel.NORMAL)
    @Tag("Validation")
    void testMalformedRequests() {
        String malformedEndpoint = "/serviceA/hello?invalid=param%00";
        APIResponse response = executeApiCall(malformedEndpoint);

        logMalformedRequestDetails(response);
        assertStatusCode(response, 400);
    }

    //$ ===>>> region Вспомогательные методы
    private APIResponse executeInvalidMethodCall(String method, String endpoint) {
        return Allure.step("Выполнение " + method + " запроса", () -> {
            APIResponse response = switch(method) {
                case "POST" -> ApiClient.post(endpoint);
                case "PUT" -> ApiClient.put(endpoint);
                case "PATCH" -> ApiClient.patch(endpoint);
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            };
            logResponseDetails(response);
            return response;
        });
    }

    private APIResponse executeApiCall(String endpoint) {
        return Allure.step("GET " + endpoint, () -> {
            APIResponse response = ApiClient.get(endpoint);
            logResponseDetails(response);
            return response;
        });
    }

    private void logResponseDetails(APIResponse response) {
        Allure.addAttachment("Response", "text/plain",
                "Status: " + response.status() + "\nBody: " + response.text());
        logger.debug("Response: {}", response);
    }

    private void logMalformedRequestDetails(APIResponse response) {
        Allure.addAttachment("Malformed Request", "text/plain",
                "Status: " + response.status() + "\nBody: " + response.text());
        logger.debug("Malformed request details:\n{}", response.text());
    }

    private void assertStatusCode(APIResponse response, int expected) {
        assertEquals(expected, response.status(), "Неверный статус код");
    }
}
