package e2e;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorHandlingTest extends TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingTest.class);


    @ParameterizedTest
    @CsvSource({
            "POST, /serviceA/hello",
            "PUT, /serviceA/hello",
            "PATCH, /serviceA/hello"
    })
    void testInvalidHttpMethods(String method, String endpoint) {
        APIResponse response = switch(method) {
            case "POST" -> ApiClient.post(endpoint);
            case "PUT" -> ApiClient.put(endpoint);
            case "PATCH" -> ApiClient.patch(endpoint);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        assertEquals(405, response.status(),
                "Method %s should return 405 for %s".formatted(method, endpoint));
    }

    @Test
    void testMalformedRequests() {
        APIResponse response = ApiClient.get("/serviceA/hello?invalid=param%00");

        logger.debug("Malformed request response:\nStatus: {}\nBody: {}",
                response.status(),
                response.text()
        );

        assertEquals(400, response.status());
    }
}