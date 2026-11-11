package com.dovewing.model;

import com.dovewing.pricing.ChargingRules;
import com.dovewing.pricing.StandardChargingRules;
import com.dovewing.service.Depot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ModelValidationTest {
    private Vehicle vehicle() {
        return new Vehicle("Car", "REG", 150, 500, StandardChargingRules.ECONOMY);
    }

    private Renter renter() {
        return new Renter("Customer", "CASUAL");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void missingTextIsRejectedWithoutConsumingEntityIds(String text) {
        Vehicle beforeVehicle = vehicle();
        Renter beforeRenter = renter();
        assertThrows(IllegalArgumentException.class, () -> new Depot(text));
        assertThrows(IllegalArgumentException.class, () -> new Vehicle(text, "REG", 1, 1, StandardChargingRules.ECONOMY));
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("Car", text, 1, 1, StandardChargingRules.ECONOMY));
        assertThrows(IllegalArgumentException.class, () -> new Renter(text, "CASUAL"));
        assertEquals(beforeVehicle.getId() + 1, vehicle().getId());
        assertEquals(beforeRenter.getId() + 1, renter().getId());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "member", "OTHER"})
    void membershipMustBeOneOfTheRecognisedValues(String membership) {
        assertThrows(IllegalArgumentException.class, () -> new Renter("Customer", membership));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidMoneyIsRejectedWithoutConsumingRentalIds(double invalidAmount) {
        Vehicle car = vehicle();
        Renter customer = renter();
        Rental before = new Rental(customer, car, 1, 150, 500);
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("Car", "REG", invalidAmount, 1, StandardChargingRules.ECONOMY));
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("Car", "REG", 1, invalidAmount, StandardChargingRules.ECONOMY));
        assertThrows(IllegalArgumentException.class, () -> new Rental(customer, car, 1, invalidAmount, 1));
        assertThrows(IllegalArgumentException.class, () -> new Rental(customer, car, 1, 1, invalidAmount));
        assertEquals(before.getId() + 1, new Rental(customer, car, 1, 150, 500).getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void invalidDaysAreRejectedForDirectConstructionAndPricing(int days) {
        Vehicle car = vehicle();
        Renter customer = renter();
        assertThrows(IllegalArgumentException.class, () -> car.calculateCost(days));
        assertThrows(IllegalArgumentException.class, () -> new Rental(customer, car, days, 1, 1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void policyCategoryMustContainText(String category) {
        ChargingRules policy = new ChargingRules() {
            public String category() { return category; }
            public double calculateCost(double rate, int days) { return 0; }
            public double calculateDeposit(double deposit, Renter renter) { return 0; }
        };
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("Car", "REG", 1, 1, policy));
    }

    @Test
    void missingObjectsAreRejected() {
        Vehicle car = vehicle();
        Renter customer = renter();
        assertThrows(NullPointerException.class, () -> new Vehicle("Car", "REG", 1, 1, null));
        assertThrows(NullPointerException.class, () -> new Rental(null, car, 1, 1, 1));
        assertThrows(NullPointerException.class, () -> new Rental(customer, null, 1, 1, 1));
        assertThrows(NullPointerException.class, () -> car.calculateDeposit(null));
    }

    @Test
    void zeroAmountsAndLargePositiveDurationsRemainValid() {
        Vehicle free = new Vehicle("Free", "FREE", 0, 0, StandardChargingRules.ECONOMY);
        Renter customer = renter();
        assertEquals(0, free.calculateCost(Integer.MAX_VALUE));
        assertEquals(0, free.calculateDeposit(customer));
        Rental rental = new Rental(customer, free, Integer.MAX_VALUE, 0, 0);
        assertEquals(Integer.MAX_VALUE, rental.getDays());
    }

    @Test
    void costOverflowIsRejected() {
        Vehicle car = new Vehicle("Car", "REG", Double.MAX_VALUE, 1, StandardChargingRules.ECONOMY);
        assertThrows(IllegalArgumentException.class, () -> car.calculateCost(2));
    }
}
