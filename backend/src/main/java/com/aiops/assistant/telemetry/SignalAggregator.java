package com.aiops.assistant.telemetry;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class SignalAggregator {

    private final List<TelemetryProvider> telemetryProviders;

    public SignalAggregator(List<TelemetryProvider> telemetryProviders) {
        this.telemetryProviders = telemetryProviders;
    }

    public List<Signal> getSignals() {
        return telemetryProviders.stream()
                .flatMap(provider -> provider.getSignals().stream())
                .sorted((left, right) -> right.observedAt().compareTo(left.observedAt()))
                .collect(Collectors.toList());
    }
}
