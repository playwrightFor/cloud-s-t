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
    protected static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private long startTime;

    @BeforeEach
    void setupPerTest() {
        ApiClient.setup(TestConfig.getGatewayUrl());
        startTime = System.currentTimeMillis();

        Allure.label("Build Version", TestConfig.getBuildVersion());
        Allure.label("Environment", TestConfig.getEnvironment());
        Allure.link("Gateway Docs", TestConfig.getGatewayDocsUrl());
        Allure.suite("Gateway Tests");
    }

    @AfterEach
    void teardownPerTest() {
        ApiClient.teardown();
        long durationMillis = System.currentTimeMillis() - startTime;
        double durationSeconds = durationMillis / 1000.0;

        String message = String.format("Выполнено за: %.2fs", durationSeconds);
        Allure.addAttachment("Test Duration", "text/plain",
                message);
        logger.info(message);
    }
}