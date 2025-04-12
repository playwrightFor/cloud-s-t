package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import utils.ApiClient;
import utils.TestConfig;

@Epic("API Gateway Тестирование")
public class TestRunner {

    @BeforeAll
    static void globalSetup() {
        ApiClient.setup(TestConfig.getGatewayUrl());

        Allure.link("Документация Gateway", "https://wiki.company.com/gateway");
        Allure.label("environment", TestConfig.getEnvironment());
        Allure.description("Базовый класс для тестов Gateway");
    }

    @AfterAll
    static void globalTeardown() {
        ApiClient.teardown();

        Allure.addAttachment("Финальный статус", "text/plain", "Все тесты завершены");
    }
}