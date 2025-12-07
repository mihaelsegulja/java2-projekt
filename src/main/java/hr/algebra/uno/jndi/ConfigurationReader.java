package hr.algebra.uno.jndi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationReader {
    private static final Properties properties;
    static {
        properties = new Properties();
        try (InputStream is = ConfigurationReader.class.getResourceAsStream("/hr/algebra/uno/app.conf")) {
            if (is == null) {
                throw new RuntimeException("Cannot find app.conf in classpath");
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static String getStringValueForKey(ConfigurationKey key) {
        return properties.getProperty(key.getKey());
    }

    public static Integer getIntegerValueForKey(ConfigurationKey key) {
        return Integer.parseInt(getStringValueForKey(key));
    }
}
