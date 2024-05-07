public class Address {
    private String street, neighbourhood, number, district, city, country;

    private double latitude, longitude;

    Address(String neighbourhood, String street, String number, String district, String city, String country, double latitude, double longitude) {
        this.neighbourhood = neighbourhood;
        this.street = street;
        this.number = number;
        this.district = district;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toString() {
        return neighbourhood + " " + street + " No: " + number + ", " + district + "/" + city + " " + country;
    }
}
