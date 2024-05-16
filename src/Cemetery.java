import java.util.*;
import java.util.concurrent.CancellationException;

public class Cemetery {
    private Address address;

    private String name, id;

    private int count;

    private final int CAPACITY = 20000;
    public static final List<ConsoleReader.Question> QUESTIONS = List.of(
            new ConsoleReader.Question("ID", "[0-9]{2}-[0-9]{3}", "Invalid ID. Must be in the format 'XX-XXX", true),
            new ConsoleReader.Question("Name", "^[\\p{L}\\p{M}'-]{2,64}$", "Invalid name. Must contain only letters, 2-64 characters.", true)
    );

    public Cemetery(final Scanner scanner, final Map<String, Cemetery> cemeteries) throws CancellationException {
        ConsoleReader reader = new ConsoleReader(scanner);

        String id;
        for (id = reader.getAnswer(QUESTIONS.get(0)); cemeteries.containsKey(id); id = reader.getAnswer(QUESTIONS.get(0))) {
            System.out.println("ID already exists. Please enter a different ID.");
        }
        this.id = id;
        this.name = reader.getAnswer(QUESTIONS.get(1));
        this.address = new Address(scanner);
    }

    public Cemetery(String id, String name, Address address) {
        this.address = address;
        this.name = name;
        this.id = id;
        this.count = 0;
    }

    Cemetery() {

    }

    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Address: " + address;
    }

    public static String toCsvHeader() {
        return "id,name,country,city,district,neighbourhood,street,latitude,longitude";
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                id == null ? "" : id,
                name == null ? "" : name,
                address.getCountry() == null ? "" : address.getCountry(),
                address.getCity() == null ? "" : address.getCity(),
                address.getDistrict() == null ? "" : address.getDistrict(),
                address.getNeighbourhood() == null ? "" : address.getNeighbourhood(),
                address.getStreet() == null ? "" : address.getStreet(),
                address.getLatitude() == 0 ? "" : address.getLatitude(),
                address.getLongitude() == 0 ? "" : address.getLongitude()
        );
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incrementCount() {
        count++;
    }

    public void decrementCount() {
        count--;
    }
}
