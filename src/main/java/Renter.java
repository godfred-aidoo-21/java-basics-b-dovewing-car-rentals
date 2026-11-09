import java.util.*;

class Renter {
    private final String name;
    private final String kind; // "CASUAL" or "MEMBER"
    private final int id;
    private final List<String> history = new ArrayList<>();
    private static int counter = 1;

    public Renter(String name, String kind) {
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

    public List<String> getHistory() {
        return List.copyOf(history);
    }

    void recordRental(String vehicleModel) {
        history.add("Rented " + vehicleModel);
    }
}

