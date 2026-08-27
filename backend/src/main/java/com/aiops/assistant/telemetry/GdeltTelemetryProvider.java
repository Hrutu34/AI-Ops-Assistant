package com.aiops.assistant.telemetry;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
@ConditionalOnProperty(name = "telemetry.gdelt.enabled", havingValue = "true")
public class GdeltTelemetryProvider implements TelemetryProvider {

    private static final DateTimeFormatter GDELT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String query;
    private final int maxRecords;

    public GdeltTelemetryProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            org.springframework.core.env.Environment environment) {
        this.restClient = restClientBuilder.baseUrl("https://api.gdeltproject.org").build();
        this.objectMapper = objectMapper;
        this.query = environment.getProperty("telemetry.gdelt.query", "conflict OR war OR crisis");
        this.maxRecords = environment.getProperty("telemetry.gdelt.max-records", Integer.class, 10);
    }

    @Override
    public List<Signal> getSignals() {
        String response;
        try {
            response = restClient.get()
                    .uri(UriComponentsBuilder.fromPath("/api/v2/doc/doc")
                            .queryParam("query", query)
                            .queryParam("mode", "artlist")
                            .queryParam("format", "json")
                            .queryParam("maxrecords", maxRecords)
                            .queryParam("sort", "datedesc")
                            .build()
                            .toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            return List.of();
        }

        try {
            return parseSignals(response);
        } catch (Exception exception) {
            return List.of();
        }
    }

    private List<Signal> parseSignals(String response) throws Exception {
        JsonNode articles = objectMapper.readTree(response).path("articles");
        List<Signal> signals = new ArrayList<>();
        for (JsonNode article : articles) {
            String title = article.path("title").asText("Untitled report");
            String url = article.path("url").asText(null);
            String domain = article.path("domain").asText("GDELT");
            signals.add(new Signal(
                    "gdelt",
                    "NEWS",
                    title,
                    "INFO",
                    parseObservedAt(article.path("seendate").asText(null)),
                    Map.of("domain", domain, "query", query),
                    url));
        }
        return signals;
    }

    private Instant parseObservedAt(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        try {
            return LocalDateTime.parse(value, GDELT_DATE_FORMAT).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException exception) {
            return Instant.now();
        }
    }
}
