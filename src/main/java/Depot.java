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
        if (!isAvailable(v)) {
            throw new IllegalStateException("Vehicle " + v.getReg() + " already has an active rental.");
        }
        double cost = v.calculateCost(days);
        double deposit = v.calculateDeposit(r);
        System.out.println("Rented " + v.getModel() + " (" + v.getReg() + ") to " + r.getName()
                + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        r.recordRental(v.getModel());
        rentals.add(new Rental(r, v, days, cost, deposit));
    }

    public void returnVehicle(Vehicle v) {
        findActiveRental(v).ifPresent(activeRental ->
                rentals.set(rentals.indexOf(activeRental), activeRental.returned()));
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

    public boolean isAvailable(Vehicle vehicle) {
        return findActiveRental(vehicle).isEmpty();
    }

    private Optional<Rental> findActiveRental(Vehicle vehicle) {
        return rentals.stream()
                .filter(rental -> rental.getVehicleId() == vehicle.getId()
                        && rental.getStatus().equals("ACTIVE"))
                .findFirst();
    }
}
