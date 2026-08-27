package com.aiops.assistant.telemetry;

import java.time.Instant;
import java.util.Map;

public record Signal(
        String source,
        String category,
        String title,
        String severity,
        Instant observedAt,
        Map<String, Object> data,
        String sourceUrl) {
}
