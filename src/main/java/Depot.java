import java.util.*;

class Depot {
    public String name;
    public List<Vehicle> vehicles = new ArrayList<>();
    public List<Renter> renters = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();

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
        double cost = 0, deposit = 0;
        if (v.category.equals("ECONOMY")) {
            cost = v.dailyRate * days;
            deposit = v.deposit;
            if (r.kind.equals("MEMBER")) {
                deposit = 0; // members have their deposit waived
            }
            v.status = "RENTED";
            System.out.println("Rented " + v.model + " (" + v.reg + ") to " + r.name
                    + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        } else if (v.category.equals("PREMIUM")) {
            cost = v.dailyRate * days;
            deposit = v.deposit;
            if (r.kind.equals("MEMBER")) {
                deposit = 0; // members have their deposit waived
            }
            v.status = "RENTED";
            System.out.println("Rented " + v.model + " (" + v.reg + ") to " + r.name
                    + " for " + days + " days, cost GHS " + cost + ", deposit GHS " + deposit);
        }
        r.history.add("Rented " + v.model);
        rentals.add(new Rental(r.name, v.reg, days, cost, deposit));
    }

    public void returnVehicle(Vehicle v) {
        v.status = "AVAILABLE";
        for (Rental rt : rentals) {
            if (rt.vehicleReg.equals(v.reg) && rt.status.equals("ACTIVE")) {
                rt.status = "RETURNED";
            }
        }
        System.out.println("Returned " + v.model + " (" + v.reg + ")");
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public void printReport() {
        RentalReport.print(this);
    }
}
