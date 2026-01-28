package ru.gtu.azure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.gtu.azure.model.ContractAlert;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

public class OpenAIService {
    private static final String ENDPOINT_ENV = "AZURE_OPENAI_ENDPOINT";
    private static final String KEY_ENV = "AZURE_OPENAI_KEY";
    private static final String DEPLOYMENT_ENV = "AZURE_OPENAI_DEPLOYMENT";
    private static final String API_VERSION = "2024-02-15-preview";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Logger logger;

    public OpenAIService(ObjectMapper objectMapper, Logger logger) {
        this.objectMapper = objectMapper;
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String buildHumanMessage(ContractAlert alert) {
        String endpoint = System.getenv(ENDPOINT_ENV);
        String apiKey = System.getenv(KEY_ENV);
        String deployment = System.getenv(DEPLOYMENT_ENV);

        if (endpoint == null || apiKey == null || deployment == null) {
            logger.info("OpenAI env vars missing; using fallback message.");
            return fallbackMessage(alert);
        }

        try {
            String requestBody = buildRequest(alert);
            String uri = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                    endpoint, deployment, API_VERSION);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warning("OpenAI request failed: " + response.statusCode() + " " + response.body());
                return fallbackMessage(alert);
            }
            return extractMessage(response.body());
        } catch (IOException e) {
            logger.warning("OpenAI call failed: " + e.getMessage());
            return fallbackMessage(alert);
        } catch (InterruptedException e) {
            logger.warning("OpenAI call interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return fallbackMessage(alert);
        }
    }

    private String buildRequest(ContractAlert alert) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode messages = root.putArray("messages");

        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", "Ты помощник, который пишет короткие и понятные уведомления о договорах.");

        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", String.format(
                "Сформируй напоминание: до окончания договора %s с %s осталось %d дней. " +
                        "Продление %s. Сделай текст дружелюбным и конкретным.",
                alert.getContractNumber(),
                alert.getCustomerName(),
                alert.getDaysRemaining(),
                alert.isAutoRenewal() ? "предусмотрено" : "не предусмотрено"
        ));

        root.put("temperature", 0.4);
        root.put("max_tokens", 200);
        return objectMapper.writeValueAsString(root);
    }

    private String extractMessage(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            JsonNode message = choices.get(0).path("message").path("content");
            if (!message.isMissingNode()) {
                return message.asText();
            }
        }
        return "Не удалось получить ответ от модели.";
    }

    private String fallbackMessage(ContractAlert alert) {
        return String.format("Через %d дней заканчивается договор №%s с %s. %s",
                alert.getDaysRemaining(),
                alert.getContractNumber(),
                alert.getCustomerName(),
                alert.isAutoRenewal()
                        ? "Продление предусмотрено — проверьте детали."
                        : "Продление не предусмотрено — стоит связаться с клиентом.");
    }
}
