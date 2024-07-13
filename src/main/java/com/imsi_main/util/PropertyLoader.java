package com.imsi_main.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        try (InputStream input = PropertyLoader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + filePath);
                return null;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }
}
