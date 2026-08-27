package com.aiops.assistant.telemetry;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MockTelemetryProvider implements TelemetryProvider {

    @Override
    public List<Signal> getSignals() {
        Instant now = Instant.now();
        return List.of(
                new Signal(
                        "demo-system",
                        "SYSTEM",
                        "Application health is nominal",
                        "INFO",
                        now,
                        Map.of("availability", 99.98),
                        null),
                new Signal(
                        "demo-market",
                        "MARKET",
                        "Market watchlist is stable",
                        "INFO",
                        now,
                        Map.of("symbol", "SPY", "changePercent", 0.7),
                        null),
                new Signal(
                        "demo-news",
                        "NEWS",
                        "Telemetry demo is ready for a live provider",
                        "INFO",
                        now,
                        Map.of("provider", "mock"),
                        null));
    }
}
