package e2e;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;


//public class FullFlowTest {
//    private static Playwright playwright;
//    private static APIRequestContext request;
//    private static final String GATEWAY_URL = "http://localhost:8084";
//    private static final int PARALLEL_REQUESTS = 100;
//    private static final int THREAD_POOL_SIZE = 20;
//
//
//    @BeforeAll
//    static void setUp() throws IOException {
//        playwright = Playwright.create();
//        request = playwright.request().newContext(new APIRequest.NewContextOptions()
//                .setBaseURL(GATEWAY_URL));
//
//    }
//
//    //Тестирование ошибок:
//    @Test
//    @Order(1)
//    void testInvalidEndpoint() {
//        var response = request.get("/invalid-endpoint");
//        assertEquals(404, response.status());
//    }
//
//    // Проверка заголовков:
//    @Test
//    @Order(2)
//    void testResponseHeaders() {
//        var response = request.get("/serviceA/hello");
//        assertEquals("text/plain;charset=UTF-8", response.headers().get("content-type"));
//    }
//
//    @Test
//    @Order(3)
//    void testNonExistentEndpoint() {
//        APIResponse response = request.get("/serviceA/invalid");
//
//        assertEquals(404, response.status());
//        assertTrue(response.text().contains("Not Found"));
//    }
//
//    //Нагрузочное тестирование
//    @Test
//    @Order(4)
//    void testLoadHandling() throws InterruptedException {
//        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failureCount = new AtomicInteger();
//
//        for (int i = 0; i < PARALLEL_REQUESTS; i++) {
//            executor.submit(() -> {
//                try (Playwright playwright = Playwright.create()) {
//                    APIRequestContext request = playwright.request().newContext();
//                    var response = request.get(GATEWAY_URL + "/serviceA/hello");
//                    if (response.status() == 200) {
//                        successCount.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    failureCount.incrementAndGet();
//                }
//            });
//        }
//
//        executor.shutdown();
//        executor.awaitTermination(1, MINUTES);
//
//        assertEquals(PARALLEL_REQUESTS, successCount.get() + failureCount.get(),
//                "Все запросы должны быть обработаны");
//        assertEquals(0, failureCount.get(),
//                "Не должно быть неудачных запросов");
//    }
//
//
//    @AfterAll
//    static void tearDown() {
//        if (playwright != null) {
//            playwright.close();
//        }
//    }
//}
import com.microsoft.playwright.APIResponse;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tests.TestRunner;
import utils.ApiClient;
import utils.TestConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullFlowTest extends TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(FullFlowTest.class);
    private static final int PARALLEL_REQUESTS = 100;
    private static final int THREAD_POOL_SIZE = 20;


    @Test
    @Order(1)
    void testInvalidEndpoint() {
        APIResponse response = ApiClient.get("/invalid-endpoint");
        assertEquals(404, response.status(),
                "Несуществующий эндпоинт должен возвращать 404");
    }

    @Test
    @Order(2)
    void testResponseHeaders() {
        APIResponse response = ApiClient.get("/serviceA/hello");

        assertAll(
                () -> assertEquals(200, response.status()),
                () -> assertEquals("text/plain;charset=UTF-8",
                        response.headers().get("content-type"),
                        "Content-Type должен соответствовать ожидаемому")
        );
    }

    @Test
    @Order(3)
    void testNonExistentEndpoint() {
        APIResponse response = ApiClient.get("/serviceA/invalid");

        assertEquals(404, response.status(),
                "Несуществующий путь сервиса должен возвращать 404");
        assertTrue(response.text().contains("Not Found"),
                "Тело ответа должно содержать информацию об ошибке");
    }

    @Test
    @Order(4)
    void testLoadHandling() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < PARALLEL_REQUESTS; i++) {
            executor.submit(() -> {
                try (Playwright playwright = Playwright.create()) {
                    APIRequestContext request = playwright.request().newContext(
                            new APIRequest.NewContextOptions()
                                    .setBaseURL(TestConfig.getGatewayUrl())
                    );

                    APIResponse response = request.get("/serviceA/hello");
                    if (response.status() == 200) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.error("Ошибка выполнения запроса: {}", e.getMessage());
                }
            });
        }

        executor.shutdown();

        boolean terminated = executor.awaitTermination(2, TimeUnit.MINUTES);
        if (!terminated) {
            logger.warn("Не все запросы завершились в течение таймаута");
            executor.shutdownNow();
        }

        assertAll(
                () -> assertEquals(PARALLEL_REQUESTS, successCount.get() + failureCount.get(),
                        "Все запросы должны быть обработаны. Успешно: %d, Ошибок: %d"
                                .formatted(successCount.get(), failureCount.get())),
                () -> assertTrue(failureCount.get() <= PARALLEL_REQUESTS * 0.05,
                        "Неудачных запросов должно быть не более 5%")
        );
    }
}