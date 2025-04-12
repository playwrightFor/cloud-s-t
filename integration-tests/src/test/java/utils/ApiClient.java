package utils;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static APIRequestContext request;
    private static Playwright playwright;

    public static void setup(String baseUrl) {
        logger.debug("Initializing Playwright with base URL: {}", baseUrl);
        playwright = Playwright.create();
        request = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(baseUrl)
                        .setTimeout(TestConfig.getRequestTimeout())
        );
    }

    public static APIResponse get(String endpoint) {
        if (TestConfig.isDebug()) {
            logger.debug("Request details: {}", request);
        }
        return request.get(endpoint);
    }
    public static APIResponse post(String endpoint) {
        logger.info("POST {}", endpoint);
        return request.post(endpoint);
    }
    public static APIResponse put(String endpoint) {
        logger.info("PUT {}", endpoint);
        return request.put(endpoint);
    }
    public static APIResponse patch(String endpoint) {
        logger.info("PATCH {}", endpoint);
        return request.patch(endpoint);
    }

    public static void teardown() {
        if (request != null) request.dispose();
        if (playwright != null) playwright.close();
    }
}