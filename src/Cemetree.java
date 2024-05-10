import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Cemetree {
    Map<String, Person> people;

    Map<String, Cemetery> cemeteries;
    private Person selectedPerson;

    public Cemetree() {
        people = new HashMap<>();
        cemeteries = new HashMap<>();
    }

    public void addPerson(Person person) {
        people.put(person.getId(), person);
    }

    public Person getPerson(String id) {
        return people.get(id);
    }

    public void updatePerson(Person person) {
        people.put(person.getId(), person);
    }

    public void removePerson(String id) {
        people.remove(id);
    }

    public void addCemetery(Cemetery cemetery) {
        cemeteries.put(cemetery.getId(), cemetery);
    }

    public Cemetery getCemetery(String id) {
        return cemeteries.get(id);
    }

    public void updateCemetery(Cemetery cemetery) {
        cemeteries.put(cemetery.getId(), cemetery);
    }

    public void removeCemetery(String id) {
        cemeteries.remove(id);
    }

    public void saveToFile(String peopleFileName, String cemeteriesFileName) throws IOException {
        FileWriter writer;

        // Save cemeteries
        writer = new FileWriter(cemeteriesFileName);

        writer.write(Cemetery.toCsvHeader() + "\n");
        for (Cemetery cemetery : cemeteries.values())
            writer.write(cemetery.toCsvString() + "\n");
        writer.close();

        // Save people
        writer = new FileWriter(peopleFileName);

        // Sort people by birthdate
        List<Person> peopleList = new ArrayList<>(people.values());
        peopleList.sort(Person::compareTo);

        writer.write(Person.toCsvHeader() + "\n");
        for (Person person : peopleList)
            writer.write(person.toCsvString() + "\n");
        writer.close();
    }

    public void loadFromFile(String peopleFileName, String cemeteriesFileName) throws IOException {
        BufferedReader reader;
        String line;

        // Load cemeteries
        reader = new BufferedReader(new FileReader(cemeteriesFileName));

        // skip header
        reader.readLine();

        line = reader.readLine();
        while (line != null) {
            String[] data = line.split(",");
            Address address = new Address(data[2], data[3], data[4], data[5], data[6], Double.parseDouble(data[7]), Double.parseDouble(data[8]));
            Cemetery cemetery = new Cemetery(data[0], data[1], address);
            cemeteries.put(cemetery.getId(), cemetery);
            line = reader.readLine();
        }

        // Load people
        reader = new BufferedReader(new FileReader(peopleFileName));

        // skip header
        reader.readLine();

        line = reader.readLine();
        while (line != null) {
            String[] data = line.split(",");

            Person person = new Person(data[0], data[1], data[2], data[3], !data[4].equals("0"), !data[5].equals("0"), data[6],
                    cemeteries.get(data[7]),
                    !data[8].isBlank() ? new Date(data[8]) : null,
                    !data[9].isBlank() ? new Date(data[9]) : null,
                    data[10].isBlank() ? null : data[10],
                    data[11].isBlank() ? null : data[11],
                    data.length == 13 ? data[12] : null
            );

            if (person.hasMotherId()) {
                Person mother = people.get(person.getMotherId());
                person.setMother(mother);
                mother.addChild(person);
            }
            if (person.hasFatherId()) {
                Person father = people.get(person.getFatherId());
                person.setFather(father);
                father.addChild(person);
            }
            if (person.hasSpouseId()) {
                Person spouse = people.get(person.getSpouseId());
                if (spouse != null) {
                    person.setSpouse(spouse);
                    spouse.setSpouse(person);
                }
            }

            people.put(person.getId(), person);

            line = reader.readLine();
        }

        // Test
        Person person = people.get("46266354792");
        System.out.println(person.getBirthDate());

        System.out.println(person);
        System.out.println("Spouse: " + person.getSpouse());
        System.out.println("Father: " + person.getFather());
        System.out.println("Mother: " + person.getMother());
        System.out.println("Children: " + person.getChildren());

    }

    public void setSelectedPerson(Person person) {
        this.selectedPerson = person;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public List<Person> searchPeopleByFilter(Person person) {
        return null;
    }

    public List<Person> searchPeopleByDate(Date startDate, Date endDate) {

        List<Person> result = new ArrayList<>();
        if (startDate != null && endDate != null) {
            for (Person person : people.values()) {
                if (person.getBirthDate() != null && person.getDeathDate() != null && person.getBirthDate().after(startDate) && person.getDeathDate().before(endDate)) {
                    result.add(person);
                }
            }
        }
        //Printing for test
        for (Person person : result) {
            System.out.println(person.getName() + " " + person.getSurname());
        }
        return result;
    }

    public List<Person> searchRelatives(int generationInterval) {
        return null;
    }
}
