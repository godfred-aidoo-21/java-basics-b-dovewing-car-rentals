class Rental {
    private final int id;
    private final String renterName;
    private final String vehicleReg;
    private final int days;
    private final double cost;
    private final double deposit;
    private final String status; // "ACTIVE" or "RETURNED"
    private static int counter = 1;

    public Rental(String renterName, String vehicleReg, int days, double cost, double deposit) {
        this.id = counter;
        counter++;
        this.renterName = renterName;
        this.vehicleReg = vehicleReg;
        this.days = days;
        this.cost = cost;
        this.deposit = deposit;
        this.status = "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public String getRenterName() {
        return renterName;
    }

    public String getVehicleReg() {
        return vehicleReg;
    }

    public int getDays() {
        return days;
    }

    public double getCost() {
        return cost;
    }

    public double getDeposit() {
        return deposit;
    }

    public String getStatus() {
        return status;
    }

    private Rental(Rental original, String status) {
        this.id = original.id;
        this.renterName = original.renterName;
        this.vehicleReg = original.vehicleReg;
        this.days = original.days;
        this.cost = original.cost;
        this.deposit = original.deposit;
        this.status = status;
    }

    Rental returned() {
        return new Rental(this, "RETURNED");
    }
}

