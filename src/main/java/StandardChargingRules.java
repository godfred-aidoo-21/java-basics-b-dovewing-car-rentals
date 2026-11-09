enum StandardChargingRules implements ChargingRules {
    ECONOMY,
    PREMIUM;

    @Override
    public String category() {
        return name();
    }

    @Override
    public double calculateCost(double dailyRate, int days) {
        return dailyRate * days;
    }

    @Override
    public double calculateDeposit(double standardDeposit, Renter renter) {
        return renter.kind.equals("MEMBER") ? 0 : standardDeposit;
    }
}
