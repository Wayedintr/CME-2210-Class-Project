public class Cemetery {
    private Address address;

    private String name;

    private int count, capacity;

    public Cemetery(Address address, String name, int capacity) {
        this.address = address;
        this.name = name;
        this.count = 0;
        this.capacity = capacity;
    }
}
