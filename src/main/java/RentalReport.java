class RentalReport {
    public static void print(Depot depot) {
        System.out.println();
        System.out.println("=== " + depot.name + " - end of day ===");
        System.out.println("Fleet status:");
        for (Vehicle v : depot.vehicles) {
            System.out.println("  " + v.model + " (" + v.reg + ", " + v.category + "): " + v.status);
        }
        System.out.println("Active rentals:");
        for (Rental rt : depot.getRentals()) {
            if (rt.status.equals("ACTIVE")) {
                System.out.println("  Rental #" + rt.id + ": " + rt.vehicleReg + " -> " + rt.renterName
                        + " (" + rt.days + " days)");
            }
        }
    }
}
