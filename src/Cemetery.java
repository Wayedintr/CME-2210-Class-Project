public class Cemetery {
    private Address address;

    private String name, id;

    private int count, capacity;

    public Cemetery(Address address, String name, String id, int capacity) {
        this.address = address;
        this.name = name;
        this.id = id;
        this.count = 0;
        this.capacity = capacity;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void incrementCount() {
        count++;
    }
}
