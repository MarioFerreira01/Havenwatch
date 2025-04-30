package com.havenwatch.models;

import java.time.LocalDateTime;

public class Alert {
    private int alertId;
    private int residentId;
    private AlertType alertType;
    private AlertSeverity severity;
    private String message;
    private AlertStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer resolvedBy;

    public enum AlertType {
        HEALTH, ENVIRONMENT, SYSTEM
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED
    }

    // Default constructor
    public Alert() {
    }

    // Constructor with essential fields
    public Alert(int residentId, AlertType alertType, AlertSeverity severity, String message) {
        this.residentId = residentId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.status = AlertStatus.ACTIVE;
    }

    // Full constructor
    public Alert(int alertId, int residentId, AlertType alertType, AlertSeverity severity,
                 String message, AlertStatus status, LocalDateTime createdAt,
                 LocalDateTime resolvedAt, Integer resolvedBy) {
        this.alertId = alertId;
        this.residentId = residentId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    // Getters and Setters
    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public int getResidentId() {
        return residentId;
    }

    public void setResidentId(int residentId) {
        this.residentId = residentId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Integer resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    @Override
    public String toString() {
        return "Alert [alertId=" + alertId + ", residentId=" + residentId +
                ", type=" + alertType + ", severity=" + severity +
                ", status=" + status + ", createdAt=" + createdAt + "]";
    }
}