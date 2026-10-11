package com.aruvi.dms.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ValidationUtil centralizes backend data validation rules.
 * Produces structured HTTP 400 error response payloads on failure.
 */
public class ValidationUtil {

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String toErrorJson() {
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("error", errorMessage);
            return JsonUtil.toJson(map);
        }
    }

    public static ValidationResult validateSeverity(int severity) {
        if (severity < 1 || severity > 5) {
            return ValidationResult.fail("Severity must be an integer between 1 and 5 (5 highest). Got: " + severity);
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validatePopulation(int population) {
        if (population <= 0) {
            return ValidationResult.fail("Affected population must be greater than zero. Got: " + population);
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            return ValidationResult.fail(fieldName + " cannot be negative. Got: " + value);
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validatePositive(int value, String fieldName) {
        if (value <= 0) {
            return ValidationResult.fail(fieldName + " must be strictly positive (> 0). Got: " + value);
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.fail(fieldName + " cannot be empty or missing.");
        }
        return ValidationResult.ok();
    }
}
