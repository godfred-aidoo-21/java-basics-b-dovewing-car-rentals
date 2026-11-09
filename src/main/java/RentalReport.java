class RentalReport {
    public static void print(Depot depot) {
        System.out.println();
        System.out.println("=== " + depot.getName() + " - end of day ===");
        System.out.println("Fleet status:");
        for (Vehicle v : depot.getVehicles()) {
            System.out.println("  " + v.getModel() + " (" + v.getReg() + ", " + v.getCategory() + "): " + v.getStatus());
        }
        System.out.println("Active rentals:");
        for (Rental rt : depot.getRentals()) {
            if (rt.getStatus().equals("ACTIVE")) {
                System.out.println("  Rental #" + rt.getId() + ": " + rt.getVehicleReg() + " -> " + rt.getRenterName()
                        + " (" + rt.getDays() + " days)");
            }
        }
    }
}
