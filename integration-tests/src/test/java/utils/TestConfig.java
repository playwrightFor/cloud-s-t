package utils;

import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("config/test.properties")) {
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test config", e);
        }
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
}
