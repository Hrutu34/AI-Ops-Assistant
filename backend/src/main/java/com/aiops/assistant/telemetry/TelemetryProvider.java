package com.aiops.assistant.telemetry;

import java.util.List;

public interface TelemetryProvider {

    List<Signal> getSignals();
}
