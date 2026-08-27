package com.aiops.assistant.telemetry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SignalStreamService {

    private final SignalAggregator signalAggregator;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SignalStreamService(SignalAggregator signalAggregator) {
        this.signalAggregator = signalAggregator;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        send(emitter, signalAggregator.getSignals());
        return emitter;
    }

    @Scheduled(
            fixedRateString = "${telemetry.stream.interval-ms:60000}",
            initialDelayString = "${telemetry.stream.initial-delay-ms:60000}")
    public void broadcastSignals() {
        List<Signal> signals = signalAggregator.getSignals();
        emitters.forEach(emitter -> send(emitter, signals));
    }

    private void send(SseEmitter emitter, List<Signal> signals) {
        try {
            emitter.send(SseEmitter.event()
                    .name("signals")
                    .data(signals));
        } catch (IOException | IllegalStateException exception) {
            emitters.remove(emitter);
        }
    }
}
