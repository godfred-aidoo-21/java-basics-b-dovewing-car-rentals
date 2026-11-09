package com.dovewing.pricing;

import com.dovewing.model.Renter;

public interface ChargingRules {
    String category();

    double calculateCost(double dailyRate, int days);

    double calculateDeposit(double standardDeposit, Renter renter);
}
