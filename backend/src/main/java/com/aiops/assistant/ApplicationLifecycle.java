package com.aiops.assistant;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLifecycle.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("AI Ops Assistant started and ready to accept requests");
    }

    @PreDestroy
    public void onApplicationStop() {
        logger.info("AI Ops Assistant stopping");
    }
}