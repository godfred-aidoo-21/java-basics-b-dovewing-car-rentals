package com.dovewing.model;

import com.dovewing.validation.InputValidation;
import com.dovewing.service.Depot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Renter {
    private final String name;
    private final String kind; // "CASUAL" or "MEMBER"
    private final int id;
    private static int counter = 1;
    private final List<Depot> depots = new ArrayList<>();

    public Renter(String name, String kind) {
        InputValidation.requireText(name, "Renter name");
        if (!"CASUAL".equals(kind) && !"MEMBER".equals(kind)) {
            throw new IllegalArgumentException("Membership must be CASUAL or MEMBER.");
        }
        this.id = counter;
        counter++;
        this.name = name;
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getId() {
        return id;
    }

    // A depot can contribute history only after it has registered this renter.
    public void registerDepot(Depot depot) {
        Objects.requireNonNull(depot, "Depot is required");
        if (!depot.getRenters().contains(this)) {
            throw new IllegalArgumentException("Renter is not registered with this depot.");
        }
        if (!depots.contains(depot)) {
            depots.add(depot);
        }
    }

    public List<String> getHistory() {
        return depots.stream()
                .flatMap(depot -> depot.getRentals().stream())
                .filter(rental -> rental.getRenterId() == id)
                .sorted(Comparator.comparingInt(Rental::getId))
                .map(rental -> "Rented " + rental.getVehicleModel())
                .toList();
    }
}

