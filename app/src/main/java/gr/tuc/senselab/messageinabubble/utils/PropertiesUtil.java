package gr.tuc.senselab.messageinabubble.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private PropertiesUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getProperty(String key, Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("config.properties")) {
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
