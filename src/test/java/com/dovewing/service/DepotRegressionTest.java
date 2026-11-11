package com.dovewing.service;

import com.dovewing.model.Rental;
import com.dovewing.model.Renter;
import com.dovewing.model.Vehicle;
import com.dovewing.pricing.ChargingRules;
import com.dovewing.pricing.StandardChargingRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepotRegressionTest {
    private Depot depot;
    private Vehicle vitz;
    private Vehicle prado;
    private Renter kofi;
    private Renter ama;

    @BeforeEach
    void setUp() {
        depot = new Depot("Dovewing");
        vitz = new Vehicle("Toyota Vitz", "GT-1234-20", 150, 500, StandardChargingRules.ECONOMY);
        prado = new Vehicle("Toyota Prado", "GR-5678-22", 600, 2000, StandardChargingRules.PREMIUM);
        kofi = new Renter("Kofi Mensah", "CASUAL");
        ama = new Renter("Ama Owusu", "MEMBER");
        depot.addVehicle(vitz);
        depot.addVehicle(prado);
        depot.addRenter(kofi);
        depot.addRenter(ama);
    }

    @ParameterizedTest(name = "{0} with {1} preserves original charges")
    @CsvSource({
            "ECONOMY, CASUAL, 150, 500, 450, 500",
            "ECONOMY, MEMBER, 150, 500, 450, 0",
            "PREMIUM, CASUAL, 600, 2000, 1800, 2000",
            "PREMIUM, MEMBER, 600, 2000, 1800, 0"
    })
    void bookingPreservesAllFourPricingCombinations(StandardChargingRules rules, String membership,
            double rate, double deposit, double expectedCost, double expectedDeposit) {
        Vehicle car = new Vehicle("Car", "PRICING", rate, deposit, rules);
        Renter customer = new Renter("Customer", membership);
        depot.addVehicle(car);
        depot.addRenter(customer);
        capture(() -> depot.rent(customer, car, 3));
        Rental rental = depot.getRentals().getFirst();
        assertEquals(expectedCost, rental.getCost());
        assertEquals(expectedDeposit, rental.getDeposit());
        assertEquals(customer.getId(), rental.getRenterId());
        assertEquals(car.getId(), rental.getVehicleId());
    }

    @Test
    void doubleBookingDoesNotChangeRecordsHistoryAvailabilityOrIds() {
        capture(() -> depot.rent(kofi, vitz, 3));
        Rental original = depot.getRentals().getFirst();
        List<Rental> before = depot.getRentals();
        List<String> history = depot.getRenterHistory(kofi);
        assertEquals("", capture(() -> assertThrows(IllegalStateException.class,
                () -> depot.rent(ama, vitz, 2))));
        assertEquals(before, depot.getRentals());
        assertEquals(history, depot.getRenterHistory(kofi));
        assertTrue(depot.getRenterHistory(ama).isEmpty());
        assertFalse(depot.isAvailable(vitz));
        assertTrue(depot.isAvailable(prado));
        capture(() -> depot.rent(ama, prado, 2));
        assertEquals(original.getId() + 1, depot.getRentals().get(1).getId());
    }

    @Test
    void duplicateBookingIsRejectedBeforeChargingRulesAreCalled() {
        int[] calls = {0};
        ChargingRules counted = new ChargingRules() {
            public String category() { return "COUNTED"; }
            public double calculateCost(double rate, int days) { calls[0]++; return rate * days; }
            public double calculateDeposit(double deposit, Renter renter) { calls[0]++; return deposit; }
        };
        Vehicle car = new Vehicle("Counted car", "COUNTED", 100, 300, counted);
        depot.addVehicle(car);
        capture(() -> depot.rent(kofi, car, 1));
        int before = calls[0];
        assertThrows(IllegalStateException.class, () -> depot.rent(ama, car, 1));
        assertEquals(before, calls[0]);
    }

    @Test
    void returnPreservesAgreementAndAllowsRerentalWithoutAffectingOtherVehicles() {
        assertTrue(depot.isAvailable(vitz));
        capture(() -> { depot.rent(kofi, vitz, 3); depot.rent(ama, prado, 2); });
        Rental original = depot.getRentals().get(0);
        Rental other = depot.getRentals().get(1);
        assertEquals("Returned Toyota Vitz (GT-1234-20)\n", capture(() -> depot.returnVehicle(vitz)));
        Rental returned = depot.getRentals().getFirst();
        assertAll(
                () -> assertEquals(original.getId(), returned.getId()),
                () -> assertEquals(kofi.getId(), returned.getRenterId()),
                () -> assertEquals(vitz.getId(), returned.getVehicleId()),
                () -> assertEquals("Kofi Mensah", returned.getRenterName()),
                () -> assertEquals("GT-1234-20", returned.getVehicleReg()),
                () -> assertEquals("Toyota Vitz", returned.getVehicleModel()),
                () -> assertEquals("ECONOMY", returned.getVehicleCategory()),
                () -> assertEquals(3, returned.getDays()),
                () -> assertEquals(450, returned.getCost()),
                () -> assertEquals(500, returned.getDeposit()),
                () -> assertEquals("RETURNED", returned.getStatus()),
                () -> assertEquals("ACTIVE", original.getStatus()),
                () -> assertSame(other, depot.getRentals().get(1)),
                () -> assertTrue(depot.isAvailable(vitz)),
                () -> assertFalse(depot.isAvailable(prado)));
        assertEquals(List.of("Rented Toyota Vitz"), depot.getRenterHistory(kofi));
        capture(() -> depot.rent(ama, vitz, 1));
        Rental next = depot.getRentals().get(2);
        assertEquals(other.getId() + 1, next.getId());
        assertEquals(0, next.getDeposit());
        assertFalse(depot.isAvailable(vitz));
        capture(() -> depot.returnVehicle(vitz));
        assertSame(returned, depot.getRentals().getFirst());
        assertEquals("RETURNED", depot.getRentals().get(2).getStatus());
    }

    @Test
    void returningRegisteredVehicleWithoutActiveRentalKeepsOriginalBehaviour() {
        assertEquals("Returned Toyota Vitz (GT-1234-20)\n", capture(() -> depot.returnVehicle(vitz)));
        assertTrue(depot.getRentals().isEmpty());
        capture(() -> { depot.rent(kofi, vitz, 1); depot.returnVehicle(vitz); });
        List<Rental> before = depot.getRentals();
        assertEquals("Returned Toyota Vitz (GT-1234-20)\n", capture(() -> depot.returnVehicle(vitz)));
        assertEquals(before, depot.getRentals());
    }

    @Test
    void collectionSnapshotsAndReturnedCopiesCannotEditDepotState() {
        List<Rental> empty = depot.getRentals();
        capture(() -> depot.rent(kofi, vitz, 1));
        assertTrue(empty.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> depot.getVehicles().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.getRenters().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.getRentals().clear());
        assertThrows(UnsupportedOperationException.class, () -> depot.getRenterHistory(kofi).clear());
        Rental original = depot.getRentals().getFirst();
        assertEquals("RETURNED", original.returned().getStatus());
        assertSame(original, depot.getRentals().getFirst());
        assertEquals("ACTIVE", original.getStatus());
        assertFalse(depot.isAvailable(vitz));
    }

    @Test
    void duplicateRegistrationAndNullEntriesLeaveCollectionsUnchanged() {
        List<Vehicle> vehicles = depot.getVehicles();
        List<Renter> renters = depot.getRenters();
        assertThrows(NullPointerException.class, () -> depot.addVehicle(null));
        assertThrows(NullPointerException.class, () -> depot.addRenter(null));
        assertThrows(IllegalArgumentException.class, () -> depot.addVehicle(vitz));
        Vehicle duplicate = new Vehicle("Other", "GT-1234-20", 1, 1, StandardChargingRules.ECONOMY);
        assertThrows(IllegalArgumentException.class, () -> depot.addVehicle(duplicate));
        assertThrows(IllegalArgumentException.class, () -> depot.addRenter(kofi));
        assertEquals(vehicles, depot.getVehicles());
        assertEquals(renters, depot.getRenters());
    }

    @Test
    void unregisteredAndNullParticipantsCannotBookOrReturn() {
        Vehicle outside = new Vehicle("Other", "GT-1234-20", 1, 1, StandardChargingRules.ECONOMY);
        Renter outsideRenter = new Renter("Kofi Mensah", "CASUAL");
        assertEquals("", capture(() -> {
            assertThrows(IllegalArgumentException.class, () -> depot.rent(kofi, outside, 1));
            assertThrows(IllegalArgumentException.class, () -> depot.rent(outsideRenter, vitz, 1));
            assertThrows(IllegalArgumentException.class, () -> depot.returnVehicle(outside));
            assertThrows(NullPointerException.class, () -> depot.rent(null, vitz, 1));
            assertThrows(NullPointerException.class, () -> depot.rent(kofi, null, 1));
            assertThrows(NullPointerException.class, () -> depot.returnVehicle(null));
        }));
        assertTrue(depot.getRentals().isEmpty());
        assertTrue(depot.getRenterHistory(kofi).isEmpty());
        assertTrue(depot.isAvailable(vitz));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void invalidDurationLeavesRecordsHistoryAndIdsUnchanged(int days) {
        capture(() -> depot.rent(kofi, vitz, 1));
        Rental original = depot.getRentals().getFirst();
        assertEquals("", capture(() -> assertThrows(IllegalArgumentException.class,
                () -> depot.rent(ama, prado, days))));
        assertEquals(List.of(original), depot.getRentals());
        assertTrue(depot.getRenterHistory(ama).isEmpty());
        assertTrue(depot.isAvailable(prado));
        capture(() -> depot.rent(ama, prado, 1));
        assertEquals(original.getId() + 1, depot.getRentals().get(1).getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidCalculatedAmountsAreRejectedWithoutPartialBooking(double invalidAmount) {
        capture(() -> depot.rent(kofi, vitz, 1));
        Rental original = depot.getRentals().getFirst();
        for (boolean badCost : new boolean[]{true, false}) {
            ChargingRules invalid = new ChargingRules() {
                public String category() { return "CUSTOM"; }
                public double calculateCost(double rate, int days) { return badCost ? invalidAmount : 1; }
                public double calculateDeposit(double deposit, Renter renter) { return badCost ? 1 : invalidAmount; }
            };
            Vehicle car = new Vehicle("Custom", "CUSTOM-" + badCost, 1, 1, invalid);
            depot.addVehicle(car);
            assertEquals("", capture(() -> assertThrows(IllegalArgumentException.class,
                    () -> depot.rent(ama, car, 1))));
            assertEquals(List.of(original), depot.getRentals());
            assertTrue(depot.getRenterHistory(ama).isEmpty());
            assertTrue(depot.isAvailable(car));
        }
        capture(() -> depot.rent(ama, prado, 1));
        assertEquals(original.getId() + 1, depot.getRentals().get(1).getId());
    }

    @Test
    void normalBookingStillPrintsOnlyTheOriginalConfirmation() {
        assertEquals("Rented Toyota Vitz (GT-1234-20) to Kofi Mensah for 3 days, cost GHS 450.0, deposit GHS 500.0\n",
                capture(() -> depot.rent(kofi, vitz, 3)));
    }


    @Test
    void renterHistoryCombinesDepotsInBookingOrderNotRegistrationOrder() {
        Depot second = new Depot("Second depot");
        second.addRenter(kofi);
        Vehicle van = new Vehicle("Van", "SECOND-1", 200, 600, StandardChargingRules.PREMIUM);
        second.addVehicle(van);
        List<String> emptySnapshot = kofi.getHistory();
        // Book at the second registered depot first to detect grouping by depot.
        capture(() -> {
            second.rent(kofi, van, 1);
            depot.rent(kofi, vitz, 2);
            second.returnVehicle(van);
            second.rent(kofi, van, 3);
        });
        List<String> expected = List.of("Rented Van", "Rented Toyota Vitz", "Rented Van");
        assertTrue(emptySnapshot.isEmpty());
        assertEquals(expected, kofi.getHistory());
        assertEquals(expected, depot.getRenterHistory(kofi));
        assertEquals(expected, second.getRenterHistory(kofi));
        assertEquals(1, depot.getRentals().size());
        assertEquals(2, second.getRentals().size());
        assertEquals("RETURNED", second.getRentals().getFirst().getStatus());
        assertTrue(ama.getHistory().isEmpty());
    }

    @Test
    void failedBookingsAndRepeatedReturnsCannotChangeCombinedHistory() {
        Depot second = new Depot("Second depot");
        second.addRenter(kofi);
        Vehicle car = new Vehicle("Second car", "SECOND-2", 100, 300, StandardChargingRules.ECONOMY);
        second.addVehicle(car);
        capture(() -> { depot.rent(kofi, vitz, 1); second.rent(kofi, car, 1); });
        List<String> before = kofi.getHistory();
        assertThrows(IllegalStateException.class, () -> second.rent(kofi, car, 1));
        assertThrows(IllegalArgumentException.class, () -> depot.rent(kofi, prado, 0));
        assertThrows(IllegalArgumentException.class, () -> second.rent(ama, car, 1));
        capture(() -> { second.returnVehicle(car); second.returnVehicle(car); });
        assertEquals(before, kofi.getHistory());
        assertEquals(before, depot.getRenterHistory(kofi));
        assertFalse(depot.isAvailable(vitz));
        assertTrue(second.isAvailable(car));
    }

    @Test
    void historySourcesRequireActualRegistrationAndCannotBeDuplicatedOrEdited() {
        Depot second = new Depot("Second depot");
        Renter sameName = new Renter("Kofi Mensah", "CASUAL");
        second.addRenter(sameName);
        assertThrows(IllegalArgumentException.class, () -> kofi.registerDepot(second));
        assertThrows(NullPointerException.class, () -> kofi.registerDepot(null));
        assertThrows(NullPointerException.class, () -> depot.getRenterHistory(null));
        kofi.registerDepot(depot);
        kofi.registerDepot(depot);
        capture(() -> depot.rent(kofi, vitz, 1));
        assertEquals(List.of("Rented Toyota Vitz"), kofi.getHistory());
        assertTrue(sameName.getHistory().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> kofi.getHistory().add("Fake rental"));
        assertThrows(UnsupportedOperationException.class, () -> second.getRenterHistory(kofi).clear());
        Rental rental = depot.getRentals().getFirst();
        rental.returned();
        assertEquals(List.of("Rented Toyota Vitz"), kofi.getHistory());
        assertEquals("ACTIVE", depot.getRentals().getFirst().getStatus());
    }

    // Console capture and shared static ID counters require sequential test execution.
    private static String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(output);
            action.run();
        } finally {
            System.setOut(original);
        }
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
