package integration;

import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.ApiClient;
import utils.TestConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceAIntegrationTest {

    @BeforeAll
    static void setUp() {
        ApiClient.setup(TestConfig.getGatewayUrl());
    }

    @Test
    void testRouting() {
        APIResponse response = ApiClient.get("/serviceA/hello");
        assertEquals(200, response.status());
    }

    @AfterAll
    static void tearDown() {
        ApiClient.teardown();
    }
}