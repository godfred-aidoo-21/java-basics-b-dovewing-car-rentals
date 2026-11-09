class Vehicle {
    private final ChargingRules chargingRules;
    public String model;
    public String reg;
    public double dailyRate;
    public double deposit;
    public String category; // "ECONOMY" or "PREMIUM"
    public String status;   // "AVAILABLE" or "RENTED"
    public int id;
    static int counter = 1;

    public Vehicle(String model, String reg, double dailyRate, double deposit, ChargingRules chargingRules) {
        this.chargingRules = java.util.Objects.requireNonNull(chargingRules, "Charging rules are required");
        this.id = counter;
        counter++;
        this.model = model;
        this.reg = reg;
        this.dailyRate = dailyRate;
        this.deposit = deposit;
        this.category = chargingRules.category();
        this.status = "AVAILABLE";
    }

    public double calculateCost(int days) {
        return chargingRules.calculateCost(dailyRate, days);
    }

    public double calculateDeposit(Renter renter) {
        return chargingRules.calculateDeposit(deposit, renter);
    }
}

