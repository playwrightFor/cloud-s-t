package integration;

import org.example.microservice2.*;
import org.example.microservice2.config.Microservice2IntegrationConfig;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Comparator;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

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

    @Autowired
    @Qualifier("microservice2FileWriterChannel")
    private MessageChannel fileWriterChannel;

    @Value("${user.requests.filepath}")
    private String logFilePath;

    @BeforeEach
    void setup() throws IOException {
        Path logDir = Paths.get(logFilePath);

        if (Files.exists(logDir)) {
            Files.walk(logDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }

        Files.createDirectories(logDir);
    }

    @Test
    void whenCallHello_ThenWriteToLog() {
        assertNotNull(fileWriterChannel, "MessageChannel должен быть инжектирован");
        assertInstanceOf(DirectChannel.class, fileWriterChannel, "Неверный тип канала");

        boolean sent = fileWriterChannel.send(MessageBuilder
                .withPayload("test_message")
                .build());

        assertTrue(sent, "Сообщение не было отправлено");

        Path logFile = Paths.get(logFilePath).resolve("requests.log");

        await().atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    assertTrue(Files.exists(logFile), "Файл лога не создан");
                    String content = Files.readString(logFile);
                    assertTrue(content.contains("test_message"),
                            "Содержимое лога: " + content);
                });
    }
}

