public class    Address {
    private String street, neighbourhood, number,district, city,country;

    Address(String neighbourhood, String street, String number, String district, String city) {
        this.neighbourhood = neighbourhood;
        this.street = street;
        this.number = number;
        this.district = district;
        this.city = city;
        this.country = "Türkiye";
    }

    public String toString() {
        return neighbourhood + " " + street + " No: " + number + ", " + district + "/" + city + " " + country;
    }
}
