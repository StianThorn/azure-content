package ru.gtu.azure.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationContext {
    private final ContractAlert contractAlert;
    private final String message;
    private final LocalDateTime createdAt;

    public NotificationContext(ContractAlert contractAlert, String message, LocalDateTime createdAt) {
        this.contractAlert = contractAlert;
        this.message = message;
        this.createdAt = createdAt;
    }

    public ContractAlert getContractAlert() {
        return contractAlert;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationContext that = (NotificationContext) o;
        return Objects.equals(contractAlert, that.contractAlert)
                && Objects.equals(message, that.message)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractAlert, message, createdAt);
    }
}
