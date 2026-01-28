package ru.gtu.azure.model;

import java.time.LocalDate;
import java.util.Objects;

public class ContractAlert {
    private final String contractNumber;
    private final String customerName;
    private final LocalDate expiresOn;
    private final boolean autoRenewal;

    public ContractAlert(String contractNumber, String customerName, LocalDate expiresOn, boolean autoRenewal) {
        this.contractNumber = contractNumber;
        this.customerName = customerName;
        this.expiresOn = expiresOn;
        this.autoRenewal = autoRenewal;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public boolean isAutoRenewal() {
        return autoRenewal;
    }

    public int getDaysRemaining() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiresOn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContractAlert that = (ContractAlert) o;
        return autoRenewal == that.autoRenewal
                && Objects.equals(contractNumber, that.contractNumber)
                && Objects.equals(customerName, that.customerName)
                && Objects.equals(expiresOn, that.expiresOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumber, customerName, expiresOn, autoRenewal);
    }

    @Override
    public String toString() {
        return "ContractAlert{" +
                "contractNumber='" + contractNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", expiresOn=" + expiresOn +
                ", autoRenewal=" + autoRenewal +
                '}';
    }
}
