package utils;

import io.qameta.allure.Epic;

import java.io.InputStream;
import java.util.Properties;

@Epic("Test Configuration Management")
public class TestConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("config/test.properties")) {
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить конфигурацию теста", e);
        }
    }

    public static String getGatewayDocsUrl() {
        return switch(getEnvironment()) {
            case "local" -> "http://localhost:8084/docs";
            case "staging" -> "https://staging-api/docs";
            default -> "https://prod-api/docs";
        };
    }

    public static String getGatewayUrl() {
        return props.getProperty("gateway.url");
    }

    public static String getEnvironment() {
        return props.getProperty("environment", "local");
    }

    public static String getServiceBEndpoint() {
        return props.getProperty("serviceb.endpoint");
    }

    public static String getServiceAEndpoint() {
        return props.getProperty("servicea.endpoint");
    }

    public static int getRequestTimeout() {
        String timeout = props.getProperty("request.timeout");
        return timeout != null ? Integer.parseInt(timeout) : 7000;
    }

    public static String getBuildVersion() {
        return props.getProperty("build.version");
    }

    public static boolean isDebug() {
        String debug = props.getProperty("debug");
        return Boolean.parseBoolean(debug);
    }
}