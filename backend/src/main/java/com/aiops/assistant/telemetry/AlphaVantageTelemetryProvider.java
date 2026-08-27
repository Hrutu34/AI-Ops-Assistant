package com.aiops.assistant.telemetry;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "telemetry.market.enabled", havingValue = "true")
public class AlphaVantageTelemetryProvider implements TelemetryProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String symbol;

    public AlphaVantageTelemetryProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            org.springframework.core.env.Environment environment) {
        this.restClient = restClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.objectMapper = objectMapper;
        this.apiKey = environment.getProperty("telemetry.market.api-key", "demo");
        this.symbol = environment.getProperty("telemetry.market.symbol", "SPY");
    }

    @Override
    public List<Signal> getSignals() {
        try {
            String response = restClient.get()
                    .uri(UriComponentsBuilder.fromPath("/query")
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build()
                            .toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            return parseSignals(response);
        } catch (RestClientException | IllegalArgumentException exception) {
            return List.of();
        }
    }

    private List<Signal> parseSignals(String response) throws IllegalArgumentException {
        try {
            JsonNode quote = objectMapper.readTree(response).path("Global Quote");
            if (quote.isMissingNode() || quote.path("05. price").isMissingNode()) {
                return List.of();
            }

            double price = quote.path("05. price").asDouble();
            double changePercent = parsePercent(quote.path("10. change percent").asText("0%"));
            String tradingDay = quote.path("07. latest trading day").asText("unknown date");
            String direction = changePercent >= 0 ? "up" : "down";

            return List.of(new Signal(
                    "alpha-vantage",
                    "MARKET",
                    String.format("%s is %s %.2f%%", symbol, direction, Math.abs(changePercent)),
                    Math.abs(changePercent) >= 2 ? "WARN" : "INFO",
                    Instant.now(),
                    Map.of(
                            "symbol", symbol,
                            "price", price,
                            "changePercent", changePercent,
                            "tradingDay", tradingDay),
                    "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol));
        } catch (Exception exception) {
            return List.of();
        }
    }

    private double parsePercent(String value) {
        return Double.parseDouble(value.replace("%", "").trim());
    }
}
