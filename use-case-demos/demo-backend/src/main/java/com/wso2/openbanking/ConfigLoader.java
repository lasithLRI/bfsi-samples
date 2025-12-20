package com.wso2.openbanking;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties prop = new Properties();
    static {
        try(InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")){
            if (inputStream == null) {
                System.out.println("Sorry, unable to find application.properties");
            }else{
                prop.load(inputStream);
            }
        }catch (Exception e){
            System.out.println("Error loading application.properties");
        }
    }

    public static String getProperty(String key){
        return prop.getProperty(key);
    }
}
