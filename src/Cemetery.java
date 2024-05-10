public class Cemetery {
    private Address address;

    private String name, id;

    private int count;

    private int capacity = 20000;

    public Cemetery(String id, String name, Address address) {
        this.address = address;
        this.name = name;
        this.id = id;
        this.count = 0;
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
