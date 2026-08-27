package com.aiops.assistant.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiops.assistant.telemetry.Signal;
import com.aiops.assistant.telemetry.TelemetryProvider;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalsController {

    private final TelemetryProvider telemetryProvider;

    public SignalsController(TelemetryProvider telemetryProvider) {
        this.telemetryProvider = telemetryProvider;
    }

    @GetMapping
    public List<Signal> signals() {
        return telemetryProvider.getSignals();
    }
}
