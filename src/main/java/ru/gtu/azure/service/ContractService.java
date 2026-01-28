package ru.gtu.azure.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.gtu.azure.model.ContractAlert;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ContractService {
    private static final String CONTRACTS_ENV = "CONTRACT_ALERTS_JSON";
    private final ObjectMapper objectMapper;
    private final Logger logger;

    public ContractService(ObjectMapper objectMapper, Logger logger) {
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    public List<ContractAlert> loadExpiringContracts() {
        String payload = System.getenv(CONTRACTS_ENV);
        if (payload == null || payload.isBlank()) {
            logger.info("CONTRACT_ALERTS_JSON is empty; no contracts to process.");
            return Collections.emptyList();
        }

        try {
            List<ContractAlertPayload> payloads = objectMapper.readValue(payload, new TypeReference<>() {});
            return payloads.stream()
                    .map(this::toAlert)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.warning("Failed to parse CONTRACT_ALERTS_JSON: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private ContractAlert toAlert(ContractAlertPayload payload) {
        LocalDate expiresOn = LocalDate.parse(payload.expiresOn());
        return new ContractAlert(payload.contractNumber(), payload.customerName(), expiresOn, payload.autoRenewal());
    }

    private record ContractAlertPayload(
            String contractNumber,
            String customerName,
            String expiresOn,
            boolean autoRenewal
    ) {
    }
}
