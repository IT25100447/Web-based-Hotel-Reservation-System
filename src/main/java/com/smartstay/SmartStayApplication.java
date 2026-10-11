package com.smartstay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SmartStayApplication {

    private final Environment environment;

    public SmartStayApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(SmartStayApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printUrl() {
        String port = environment.getProperty("server.port", "9090");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        System.out.println("\n" +
            "=========================================\n" +
            "  SmartStay is running!\n" +
            "  Local:  http://localhost:" + port + contextPath + "\n" +
            "=========================================\n"
        );
    }
}
