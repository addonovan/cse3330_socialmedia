package com.addonovan.cse3330;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // force the database connection open before so any failure is immediate
        // and not delayed until the first time the class is
        forceInit(DatabaseDriver.class);

        SpringApplication.run(Application.class, args);
    }

    private static <T> void forceInit(Class<T> clazz) {
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Failed to force class initialization!");
        }
    }

}
