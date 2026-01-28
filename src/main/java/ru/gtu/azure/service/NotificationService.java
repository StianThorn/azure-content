package ru.gtu.azure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.gtu.azure.model.NotificationContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;

public class NotificationService {
    private static final String OUTLOOK_WEBHOOK_ENV = "OUTLOOK_WEBHOOK_URL";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Logger logger;

    public NotificationService(ObjectMapper objectMapper, Logger logger) {
        this.objectMapper = objectMapper;
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendAll(NotificationContext context) {
        sendEmail(context);
    }

    private void sendEmail(NotificationContext context) {
        Optional<String> webhook = Optional.ofNullable(System.getenv(OUTLOOK_WEBHOOK_ENV));
        if (webhook.isEmpty()) {
            logger.info("OUTLOOK_WEBHOOK_URL is not set; skipping email.");
            return;
        }
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("subject", "Напоминание о договоре");
        payload.put("body", context.getMessage());
        postJson(webhook.get(), payload, "outlook email");
    }

    private void postJson(String uri, ObjectNode payload, String channel) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warning("Failed to send " + channel + " notification: " + response.statusCode()
                        + " " + response.body());
            }
        } catch (IOException e) {
            logger.warning("Failed to send " + channel + " notification: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.warning("Failed to send " + channel + " notification: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
