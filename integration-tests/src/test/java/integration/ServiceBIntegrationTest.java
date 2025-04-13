package integration;

import io.qameta.allure.*;
import org.example.microservice2.Microservice2Application;
import org.example.microservice2.config.Microservice2IntegrationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayMetricsAutoConfiguration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;


@Epic("ServiceB Тестирование")
@Feature("Интеграция с файловой системой")
@Story("Проверка записи логов запросов")
@SpringBootTest(
        classes = {
                Microservice2Application.class,
                Microservice2IntegrationConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableAutoConfiguration(exclude = {
        GatewayAutoConfiguration.class,
        GatewayMetricsAutoConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:config/test.properties")
public class ServiceBIntegrationTest {
    private static final String TEST_MESSAGE = "test_message";
    private static final String LOG_FILENAME = "requests.log";
    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(1);

    @Autowired
    @Qualifier("microservice2FileWriterChannel")
    private MessageChannel requestChannel;

    @Value("${user.requests.filepath}")
    private String logDirectoryPath;

    @BeforeEach
    void prepareLogDirectory() throws IOException {
        Path logDir = Paths.get(logDirectoryPath);
        cleanDirectory(logDir);
        Files.createDirectories(logDir);
    }

    @Test
    @DisplayName("Проверка записи сообщения в лог-файл")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("Integration")
    @Tag("FileSystem")
    void shouldWriteMessageToLogFile_whenRequestProcessed() {
        verifyChannelConfiguration();
        sendTestMessage();
        awaitLogFileCreation();
    }

    private void cleanDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(this::deleteSilently);
        }
    }

    private void deleteSilently(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private void verifyChannelConfiguration() {
        Allure.step("Проверка конфигурации канала", () -> {
            assertNotNull(requestChannel, "Канал для записи не инициализирован");
            assertInstanceOf(DirectChannel.class, requestChannel,
                    "Неверный тип канала. Ожидается DirectChannel");
        });
    }

    private void sendTestMessage() {
        Allure.step("Отправка тестового сообщения", () -> {
            boolean messageSent = requestChannel.send(
                    MessageBuilder.withPayload(TEST_MESSAGE).build()
            );
            assertTrue(messageSent, "Сообщение не было отправлено в канал");
        });
    }

    private void awaitLogFileCreation() {
        Path expectedLogFile = Paths.get(logDirectoryPath).resolve(LOG_FILENAME);

        Allure.step("Ожидание создания лог-файла", () ->
                await().atMost(AWAIT_TIMEOUT)
                        .pollInterval(POLL_INTERVAL)
                        .untilAsserted(() -> verifyLogFileContent(expectedLogFile))
        );
    }

    private void verifyLogFileContent(Path logFile) {
        Allure.step("Проверка содержимого файла", () -> {
            assertTrue(Files.exists(logFile), "Лог-файл не создан: " + logFile);

            String fileContent = readFileContent(logFile);
            assertTrue(fileContent.contains(TEST_MESSAGE),
                    "Лог не содержит тестовое сообщение. Содержимое: " + fileContent);
        });
    }

    private String readFileContent(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            fail("Ошибка чтения файла: " + e.getMessage());
            return "";
        }
    }
}