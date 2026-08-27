package com.aiops.assistant.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.aiops.assistant.telemetry.Signal;
import com.aiops.assistant.telemetry.SignalAggregator;
import com.aiops.assistant.telemetry.SignalStreamService;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalsController {

    private final SignalAggregator signalAggregator;
    private final SignalStreamService signalStreamService;

    public SignalsController(SignalAggregator signalAggregator, SignalStreamService signalStreamService) {
        this.signalAggregator = signalAggregator;
        this.signalStreamService = signalStreamService;
    }

    @GetMapping
    public List<Signal> signals() {
        return signalAggregator.getSignals();
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream() {
        return signalStreamService.subscribe();
    }
}
