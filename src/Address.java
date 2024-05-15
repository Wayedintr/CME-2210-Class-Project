import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CancellationException;

public class Address {
    private String street, neighbourhood, district, city, country;

    private double latitude, longitude;

    public static final List<ConsoleReader.Question> QUESTIONS = List.of(
            new ConsoleReader.Question("Country", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid country. Must contain only letters, 2-64 characters.", true),
            new ConsoleReader.Question("City", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid city. Must contain only letters, 2-64 characters.", true),
            new ConsoleReader.Question("District", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid district. Must contain only letters, 2-64 characters.", true),
            new ConsoleReader.Question("Neighbourhood", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid neighbourhood. Must contain only letters, 2-64 characters.", true),
            new ConsoleReader.Question("Street", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid street. Must contain only letters, 2-64 characters.", false),
            new ConsoleReader.Question("Latitude", "-?([0-8]?[0-9]|90)(\\.[0-9]{1,6})?", "Invalid latitude. Must be a number between -90.0 and 90.0.", false),
            new ConsoleReader.Question("Longitude", "-?((1?[0-7]?|[0-9]?)[0-9]|180)(\\.[0-9]{1,6})?", "Invalid longitude. Must be a number between -180.0 and 180.0.", false)
    );

    Address(final Scanner scanner) throws CancellationException {
        ConsoleReader reader = new ConsoleReader(scanner);

        this.country = reader.getAnswer(QUESTIONS.getFirst());
        this.city = reader.getAnswer(QUESTIONS.get(1));
        this.district = reader.getAnswer(QUESTIONS.get(2));
        this.neighbourhood = reader.getAnswer(QUESTIONS.get(3));
        this.street = reader.getAnswer(QUESTIONS.get(4));

        try {
            String latitudeS = reader.getAnswer(QUESTIONS.get(5));
            String longitudeS = reader.getAnswer(QUESTIONS.get(6));

            if (latitudeS.isBlank() || longitudeS.isBlank()) {
                return;
            }

            this.latitude = Double.parseDouble(latitudeS);
            this.longitude = Double.parseDouble(longitudeS);
        } catch (NumberFormatException e) {
            System.out.println("Can not parse latitude and longitude.");
        }
    }

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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
