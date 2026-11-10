package com.dovewing.model;

import com.dovewing.validation.InputValidation;

public class Rental {
    private final int id;
    private final int renterId;
    private final String renterName;
    private final int vehicleId;
    private final String vehicleReg;
    private final String vehicleModel;
    private final String vehicleCategory;
    private final int days;
    private final double cost;
    private final double deposit;
    private final String status; // "ACTIVE" or "RETURNED"
    private static int counter = 1;

    public Rental(Renter renter, Vehicle vehicle, int days, double cost, double deposit) {
        java.util.Objects.requireNonNull(renter, "Renter is required");
        java.util.Objects.requireNonNull(vehicle, "Vehicle is required");
        InputValidation.requireDays(days);
        InputValidation.requireAmount(cost, "Rental cost");
        InputValidation.requireAmount(deposit, "Rental deposit");
        this.id = counter;
        counter++;
        this.renterId = renter.getId();
        this.renterName = renter.getName();
        this.vehicleId = vehicle.getId();
        this.vehicleReg = vehicle.getReg();
        this.vehicleModel = vehicle.getModel();
        this.vehicleCategory = vehicle.getCategory();
        this.days = days;
        this.cost = cost;
        this.deposit = deposit;
        this.status = "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public int getRenterId() {
        return renterId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public String getRenterName() {
        return renterName;
    }

    public String getVehicleReg() {
        return vehicleReg;
    }

    public int getDays() {
        return days;
    }

    public double getCost() {
        return cost;
    }

    public double getDeposit() {
        return deposit;
    }

    public String getStatus() {
        return status;
    }

    private Rental(Rental original, String status) {
        this.id = original.id;
        this.renterId = original.renterId;
        this.renterName = original.renterName;
        this.vehicleId = original.vehicleId;
        this.vehicleReg = original.vehicleReg;
        this.vehicleModel = original.vehicleModel;
        this.vehicleCategory = original.vehicleCategory;
        this.days = original.days;
        this.cost = original.cost;
        this.deposit = original.deposit;
        this.status = status;
    }

    public Rental returned() {
        return new Rental(this, "RETURNED");
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleCategory() {
        return vehicleCategory;
    }
}

