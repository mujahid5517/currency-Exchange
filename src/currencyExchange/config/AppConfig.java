// File name: AppConfig.java
package currencyExchange.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;
    
    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            // Use defaults with NEW API key
            properties.setProperty("server.ip", "127.0.0.1");
            properties.setProperty("server.port", "1099");
            properties.setProperty("pia.url", "api.url=https://marketplace.apilayer.com/order_complete/currency_data/free-plan/593?txn=free");
            properties.setProperty("api.key", "api.key=GZiZz3i4ZfVrgKkekICuKLL4igml2uYb"); // NEW KEY
        }
    }
    
    public static String getServerIP() {
        return properties.getProperty("server.ip", "127.0.0.1");
    }
    
    public static String getServerPort() {
        return properties.getProperty("server.port", "1099");
    }
    
    public static String getApiUrl() {
        return properties.getProperty("api.url", "https://api.apilayer.com/fixer");
    }
    
    public static String getApiKey() {
        return properties.getProperty("api.key", "b278ad45c04b4694b770fdc60e8a8950"); // NEW KEY
    }
}