package com.dovewing.service;

import com.dovewing.model.Vehicle;
import com.dovewing.model.Renter;
import com.dovewing.model.Rental;
import com.dovewing.reporting.RentalReport;
import com.dovewing.validation.InputValidation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Depot {
    private final String name;
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Renter> renters = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();

    public Depot(String name) {
        InputValidation.requireText(name, "Depot name");
        this.name = name;
    }

    public void addVehicle(Vehicle v) {
        Objects.requireNonNull(v, "Vehicle is required");
        if (vehicles.stream().anyMatch(vehicle -> vehicle.getReg().equals(v.getReg()))) {
            throw new IllegalArgumentException("Vehicle registration already exists: " + v.getReg());
        }
        vehicles.add(v);
    }

    public void addRenter(Renter r) {
        Objects.requireNonNull(r, "Renter is required");
        if (renters.contains(r)) {
            throw new IllegalArgumentException("Renter is already registered: " + r.getId());
        }
        renters.add(r);
    }

    // Rent a vehicle to a renter for a number of days. The rules depend on the class of vehicle.
    public Rental rent(Renter r, Vehicle v, int days) {
        requireRegisteredVehicle(v);
        Objects.requireNonNull(r, "Renter is required");
        if (!renters.contains(r)) {
            throw new IllegalArgumentException("Renter is not registered with this depot.");
        }
        InputValidation.requireDays(days);
        if (!isAvailable(v)) {
            throw new IllegalStateException("Vehicle " + v.getReg() + " already has an active rental.");
        }
        double cost = v.calculateCost(days);
        double deposit = v.calculateDeposit(r);
        System.out.println("Rented " + v.getModel() + " (" + v.getReg() + ") to " + r.getName()
                + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        Rental rental = new Rental(r, v, days, cost, deposit);
        rentals.add(rental);
        return rental;
    }

    public void returnVehicle(Vehicle v) {
        requireRegisteredVehicle(v);
        findActiveRental(v).ifPresent(activeRental ->
                rentals.set(rentals.indexOf(activeRental), activeRental.returned()));
        System.out.println("Returned " + v.getModel() + " (" + v.getReg() + ")");
    }

    public List<Rental> getRentals() {
        return List.copyOf(rentals);
    }

    public void printReport() {
        RentalReport.print(this);
    }

    public String getName() {
        return name;
    }

    public List<Vehicle> getVehicles() {
        return List.copyOf(vehicles);
    }

    public List<Renter> getRenters() {
        return List.copyOf(renters);
    }

    public boolean isAvailable(Vehicle vehicle) {
        return findActiveRental(vehicle).isEmpty();
    }

    private Optional<Rental> findActiveRental(Vehicle vehicle) {
        return rentals.stream()
                .filter(rental -> rental.getVehicleId() == vehicle.getId()
                        && rental.getStatus().equals("ACTIVE"))
                .findFirst();
    }

    private void requireRegisteredVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle is required");
        if (!vehicles.contains(vehicle)) {
            throw new IllegalArgumentException("Vehicle is not registered with this depot.");
        }
    }

    public List<String> getRenterHistory(Renter renter) {
        Objects.requireNonNull(renter, "Renter is required");
        return rentals.stream()
                .filter(rental -> rental.getRenterId() == renter.getId())
                .map(rental -> "Rented " + rental.getVehicleModel())
                .toList();
    }
}
