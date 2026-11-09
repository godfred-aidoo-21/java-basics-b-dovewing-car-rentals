package com.dovewing.model;

import com.dovewing.validation.InputValidation;

public class Renter {
    private final String name;
    private final String kind; // "CASUAL" or "MEMBER"
    private final int id;
    private static int counter = 1;

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
}

