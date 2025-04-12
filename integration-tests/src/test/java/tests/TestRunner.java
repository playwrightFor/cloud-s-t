package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ApiClient;
import utils.TestConfig;

@Epic("API Gateway Тестирование")
public class TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private long startTime;

    @BeforeEach
    void setupPerTest() {
        ApiClient.setup(TestConfig.getGatewayUrl());
        startTime = System.currentTimeMillis();

        Allure.label("Environment", TestConfig.getEnvironment());
        Allure.link("Gateway Docs", TestConfig.getGatewayDocsUrl());
        Allure.suite("Gateway Tests");
    }

    @AfterEach
    void teardownPerTest() {
        ApiClient.teardown();
        long durationMillis = System.currentTimeMillis() - startTime;
        String message = "Выполнено за: " + durationMillis + "s";

        Allure.addAttachment("Test Duration", "text/plain",
                "Выполнено за: " + durationMillis + "s");
        logger.info(message);
    }
}