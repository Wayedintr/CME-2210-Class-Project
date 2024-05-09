public class Address {
    private String street, neighbourhood, district, city, country;

    private double latitude, longitude;

    Address(String country, String city, String district, String neighbourhood, String street, double latitude, double longitude) {
        this.country = country;
        this.city = city;
        this.district = district;
        this.neighbourhood = neighbourhood;
        this.street = street;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toString() {
        return neighbourhood + ", " + street + ", " + district + "/" + city + ", " + country;
    }
}
