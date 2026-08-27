package com.aiops.assistant.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiops.assistant.telemetry.Signal;
import com.aiops.assistant.telemetry.TelemetryProvider;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalsController {

    private final List<TelemetryProvider> telemetryProviders;

    public SignalsController(List<TelemetryProvider> telemetryProviders) {
        this.telemetryProviders = telemetryProviders;
    }

    @GetMapping
    public List<Signal> signals() {
        return telemetryProviders.stream()
                .flatMap(provider -> provider.getSignals().stream())
                .sorted((left, right) -> right.observedAt().compareTo(left.observedAt()))
                .collect(Collectors.toList());
    }
}
