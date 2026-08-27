package com.aiops.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiOpsAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOpsAssistantApplication.class, args);
    }
}
