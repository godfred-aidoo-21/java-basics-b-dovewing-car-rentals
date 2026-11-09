package com.dovewing;

import com.dovewing.model.Vehicle;
import com.dovewing.model.Renter;
import com.dovewing.service.Depot;
import com.dovewing.pricing.StandardChargingRules;

// Dovewing Car Rentals - fleet and rental tracker
// Demonstrates successful rentals, a return, and rejection of a double booking.


public class RentalTracker {

    public static void main(String[] args) {
        Depot d = new Depot("Dovewing Car Rentals");

        Vehicle vitz = new Vehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00, StandardChargingRules.ECONOMY);
        Vehicle prado = new Vehicle("Toyota Prado", "GR-5678-22", 600.00, 2000.00, StandardChargingRules.PREMIUM);
        d.addVehicle(vitz);
        d.addVehicle(prado);

        Renter kofi = new Renter("Kofi Mensah", "CASUAL");
        Renter ama = new Renter("Ama Owusu", "MEMBER");
        d.addRenter(kofi);
        d.addRenter(ama);

        // a normal day
        d.rent(kofi, vitz, 3);
        d.rent(ama, prado, 2);
        d.returnVehicle(prado);

        // The Vitz is still rented to Kofi, so this booking must be rejected.
        try {
            d.rent(ama, vitz, 2);
        } catch (IllegalStateException exception) {
            System.out.println("Rental rejected: " + exception.getMessage());
        }

        d.printReport();
    }
}

