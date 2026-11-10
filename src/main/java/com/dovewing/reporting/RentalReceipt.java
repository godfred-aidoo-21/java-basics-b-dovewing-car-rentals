package com.dovewing.reporting;

import com.dovewing.model.Rental;
import java.util.Locale;
import java.util.Objects;

public final class RentalReceipt {
    private RentalReceipt() {
    }

    public static void print(Rental rental) {
        Objects.requireNonNull(rental, "Rental is required");
        System.out.println();
        System.out.println("=== Dovewing Car Rentals: Rental Receipt ===");
        System.out.println("Rental ID: " + rental.getId());
        System.out.println("Renter: " + rental.getRenterName());
        System.out.println("Vehicle: " + rental.getVehicleModel());
        System.out.println("Registration: " + rental.getVehicleReg());
        System.out.println("Category: " + rental.getVehicleCategory());
        System.out.println("Duration: " + rental.getDays() + " days");
        System.out.printf(Locale.ROOT, "Rental cost: GHS %.2f%n", rental.getCost());
        System.out.printf(Locale.ROOT, "Deposit: GHS %.2f%n", rental.getDeposit());
    }
}
