package com.dovewing.model;

import com.dovewing.pricing.ChargingRules;
import com.dovewing.validation.InputValidation;

public class Vehicle {
    private final ChargingRules chargingRules;
    private final String model;
    private final String reg;
    private final double dailyRate;
    private final double deposit;
    private final String category; // "ECONOMY" or "PREMIUM"
    private final int id;
    private static int counter = 1;

    public Vehicle(String model, String reg, double dailyRate, double deposit, ChargingRules chargingRules) {
        this.chargingRules = java.util.Objects.requireNonNull(chargingRules, "Charging rules are required");
        InputValidation.requireText(model, "Vehicle model");
        InputValidation.requireText(reg, "Vehicle registration");
        InputValidation.requireAmount(dailyRate, "Daily rate");
        InputValidation.requireAmount(deposit, "Deposit");
        String category = chargingRules.category();
        InputValidation.requireText(category, "Vehicle category");
        this.id = counter;
        counter++;
        this.model = model;
        this.reg = reg;
        this.dailyRate = dailyRate;
        this.deposit = deposit;
        this.category = category;
    }

    public double calculateCost(int days) {
        InputValidation.requireDays(days);
        double cost = chargingRules.calculateCost(dailyRate, days);
        InputValidation.requireAmount(cost, "Calculated cost");
        return cost;
    }

    public double calculateDeposit(Renter renter) {
        java.util.Objects.requireNonNull(renter, "Renter is required");
        double calculatedDeposit = chargingRules.calculateDeposit(deposit, renter);
        InputValidation.requireAmount(calculatedDeposit, "Calculated deposit");
        return calculatedDeposit;
    }

    public String getModel() {
        return model;
    }

    public String getReg() {
        return reg;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public double getDeposit() {
        return deposit;
    }

    public String getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }
}

