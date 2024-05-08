import java.io.BufferedReader;
import java.io.FileReader;
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

    public int saveToFile(String fileName) {
        return 0;
    }

    public void loadFromFile(String fileName) throws IOException {
        BufferedReader reader;

        reader = new BufferedReader(new FileReader(fileName));

        String line = reader.readLine();

        // skip header
        line = reader.readLine();

        while (line != null) {
            String[] data = line.split(",");
            String[] birthDate = data[8].split("/");
            String[] deathDate = data[9].split("/");

            Person person = new Person(data[0], data[1], data[2], data[3], !data[4].equals("0"), !data[5].equals("0"), data[6],
                    null,
                    new GregorianCalendar(Integer.parseInt(birthDate[2]), Integer.parseInt(birthDate[1]), Integer.parseInt(birthDate[0])),
                    deathDate.length == 3 ? new GregorianCalendar(Integer.parseInt(deathDate[2]), Integer.parseInt(deathDate[1]), Integer.parseInt(deathDate[0])) : null,
                    data[10].equals("") ? null : data[10],
                    data[11].equals("") ? null : data[11],
                    data.length == 13 ? data[12] : null
            );

            if (data[0].equals("36071817384"))
                System.out.println("found");

            if (person.hasMotherId()) {
//                System.out.println(people.size() + " " + person.getMotherId());
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
        Person person = people.get("80609910306");

        for (Person p : people.values()) {
            if (p.getChildren().size() > 1)
                person = p;
        }

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

    public List<Person> searchPeopleByDate(GregorianCalendar startDate, GregorianCalendar endDate) {
        return null;
    }

    public List<Person> searchRelatives(int generationInterval) {
        return null;
    }
}
