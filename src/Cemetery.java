import java.util.*;
import java.util.concurrent.CancellationException;

public class Cemetery {

    public class Visit implements Comparable<Visit> {

        private final Person person;
        private final Date date;

        Visit(Person person, Date date) {
            this.person = person;
            this.date = date;
        }

        public String toString() {
            return person.getName() + " " + person.getSurname() + ", " + date.toStringCLK();
        }

        public int compareTo(Visit other) {
            return other.date.compareTo(this.date);
        }

        public Person getPerson() {
            return person;
        }

        public Date getDate() {
            return date;
        }
    }

    Map<Person, SortedSet<Visit>> visitorList = new HashMap<>();

    private Address address;

    private String name, id;

    public int count;

    public final int CAPACITY = 20000;

    public static final List<ConsoleReader.Question> QUESTIONS = List.of(
            new ConsoleReader.Question("ID", "[0-9]{2}-[0-9]{3}", "Invalid ID. Must be in the format 'XX-XXX", true),
            new ConsoleReader.Question("Name", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid name. Must contain only letters, 2-64 characters.", true)
    );

    Cemetery(String id, String name, Address address) {
        this.address = address;
        this.name = name;
        this.id = id;
        this.count = 0;
    }

    Cemetery(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Cemetery(ConsoleReader reader, Map<String, Cemetery> cemeteries) throws CancellationException {
        String id;
        for (id = reader.getAnswer(QUESTIONS.get(0)); cemeteries.containsKey(id); id = reader.getAnswer(QUESTIONS.get(0))) {
            System.out.println("ID already exists. Please enter a different ID.");
        }
        this.id = id;
        this.name = reader.getAnswer(QUESTIONS.get(1));
        this.address = new Address(reader);
    }

    public void edit(ConsoleReader reader) throws CancellationException {
        String name = reader.getAnswer(QUESTIONS.get(1).withLabel("Name (" + this.name + ")").withRequired(false), 30);

        address.edit(reader);

        this.name = name.isBlank() ? this.name : name;
    }

    public void connect(Map<String, Person> people) {
        for (Person person : people.values()) {
            if (person.getCemetery() != null && person.getCemetery().getId().equals(this.getId())) {
                person.setCemetery(this);
            }
        }
    }

    public void addVisitor(Person visitedPerson, Person visitorPerson, Date date) {
        if (visitorList.containsKey(visitedPerson)) {
            SortedSet<Visit> visits = visitorList.get(visitedPerson);
            visits.add(new Visit(visitorPerson, date));
        } else {
            SortedSet<Visit> visits = new TreeSet<>();
            visits.add(new Visit(visitorPerson, date));
            visitorList.put(visitedPerson, visits);
        }
    }

    public SortedSet<Visit> getVisitorsOfPerson(Person person) {
        return visitorList.get(person);
    }

    public Map<Person, SortedSet<Visit>> getVisitorList() {
        return visitorList;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            Map.Entry<K, V> entry = list.get(i);
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public String getStatistics(Map<String, Person> people) {
        String result = "Statistics of " + this.name + "\n";

        Map<String, Integer> deathCauses = new LinkedHashMap<>();

        double sumOfAges = 0;
        double maleCount = 0;
        double femaleCount = 0;

        double deathCount = 0;

        for (Person person : people.values()) {
            if (person.dead && person.getCemetery() != null && person.getCemetery().getId().equals(this.getId())) {
                sumOfAges += person.getAge();
                if (person.getSex().equals("Male"))
                    maleCount++;
                else if (person.getSex().equals("Female"))
                    femaleCount++;
                if (!person.getDeathCause().isBlank()) {
                    if (deathCauses.containsKey(person.getDeathCause())) {
                        deathCauses.put(person.getDeathCause(), deathCauses.get(person.getDeathCause()) + 1);
                        deathCount++;
                    } else {
                        deathCauses.put(person.getDeathCause(), 1);
                        deathCount++;
                    }
                }
            }
        }

        deathCauses = sortByValue(deathCauses);

        result += String.format("%-28s : %.2f\n", "Average age", sumOfAges / count);
        result += String.format("%-28s : %.2f%%/%.2f%%\n", "Male/Female", maleCount / count * 100.0, femaleCount / count * 100.0);

        result += "─".repeat(56) + "\n";

        result += String.format("%-28s : %.0f", "Total deaths", deathCount);
        for (Map.Entry<String, Integer> entry : deathCauses.entrySet()) {
            double percentage = entry.getValue() / deathCount * 100.0;
            if (percentage >= 1)
                result += String.format("\n%-28s : %2.0f%% %s", entry.getKey(), percentage, "▉".repeat((int) percentage));
        }

        return result;
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

    public static String toRowHeader() {
        return String.format("    %-12s %-30s %s", "ID", "Name", "Address");
    }

    public String toRowString(int index) {
        return String.format("%-3d %-12s %-30s %s", (index), id, name, address);
    }

    public String toDetailString(boolean admin) {
        return (admin ? "" :
                "ID      : " + id + "\n") +
                "Name    : " + name + "\n" +
                "Address : " + address.toString() + "\n" +
                "Coords  : " + address.getLatitude() + ", " + address.getLongitude() + "\n" +
                "Ratio   : " + String.format("%s/%s (%.0f%%)", count, CAPACITY, ((double) count / (double) CAPACITY * 100.0));
    }

    public boolean matches(Cemetery filter) {
        if (filter == null) return true;
        if (filter.id != null && !filter.id.equals(this.id)) return false;
        if (filter.name != null && !filter.name.equalsIgnoreCase(this.name)) return false;
        return true;
    }

    public static Comparator<? super Cemetery> getComparator(String sortBy) {
        if (sortBy.equals("id")) return Comparator.comparing(Cemetery::getId);
        if (sortBy.equals("name")) return Comparator.comparing(Cemetery::getName);
        if (sortBy.equals("address")) return Comparator.comparing(Cemetery::getAddressStringReverse);
        if (sortBy.equals("ratio")) return Comparator.comparing(Cemetery::getRatio);
        return Comparator.comparing(Cemetery::getId);
    }

    public double getRatio() {
        return 1 - (double) count / (double) CAPACITY;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    private String getAddressStringReverse() {
        return address.toStringReverse();
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
