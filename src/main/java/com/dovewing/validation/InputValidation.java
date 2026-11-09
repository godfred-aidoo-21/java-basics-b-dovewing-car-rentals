package com.dovewing.validation;

public final class InputValidation {
    private InputValidation() {
    }

    public static void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank.");
        }
    }

    public static void requireAmount(double value, String label) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
    }

    public static void requireDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Rental duration must be positive.");
        }
    }
}
