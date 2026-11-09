import java.util.*;

class Depot {
    private final String name;
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Renter> renters = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();

    public Depot(String name) {
        this.name = name;
    }

    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public void addRenter(Renter r) {
        renters.add(r);
    }

    // Rent a vehicle to a renter for a number of days. The rules depend on the class of vehicle.
    public void rent(Renter r, Vehicle v, int days) {
        double cost = v.calculateCost(days);
        double deposit = v.calculateDeposit(r);
        v.markRented();
        System.out.println("Rented " + v.getModel() + " (" + v.getReg() + ") to " + r.getName()
                + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        r.recordRental(v.getModel());
        rentals.add(new Rental(r.getName(), v.getReg(), days, cost, deposit));
    }

    public void returnVehicle(Vehicle v) {
        v.markAvailable();
        for (int index = 0; index < rentals.size(); index++) {
            Rental rt = rentals.get(index);
            if (rt.getVehicleReg().equals(v.getReg()) && rt.getStatus().equals("ACTIVE")) {
                rentals.set(index, rt.returned());
            }
        }
        System.out.println("Returned " + v.getModel() + " (" + v.getReg() + ")");
    }

    public List<Rental> getRentals() {
        return List.copyOf(rentals);
    }

    public void printReport() {
        RentalReport.print(this);
    }

    public String getName() {
        return name;
    }

    public List<Vehicle> getVehicles() {
        return List.copyOf(vehicles);
    }

    public List<Renter> getRenters() {
        return List.copyOf(renters);
    }
}
