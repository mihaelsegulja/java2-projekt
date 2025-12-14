package hr.algebra.uno.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

public class ConfigurationReader {

    private static Properties properties;

    static {
        properties = new Properties();
        Hashtable<String, String> configuration = new Hashtable<>();
        configuration.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        configuration.put(Context.PROVIDER_URL, "file:src/main/resources/hr/algebra/uno");

        try(InitialDirContextCloseable context = new InitialDirContextCloseable(configuration)) {
            Object configurationObject = context.lookup("app.conf");
            properties.load(new FileReader(configurationObject.toString()));
        }
        catch (NamingException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringValueForKey(ConfigurationKey key) {
        return properties.getProperty(key.getKey());
    }

    public static Integer getIntegerValueForKey(ConfigurationKey key) {
        return Integer.parseInt(getStringValueForKey(key));
    }
}
