import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;

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
            Cemetery cemetery = cemeteries.get(data[7]);
            if (cemetery != null)
                cemetery.incrementCount();

            Person person = new Person(data[0], data[1], data[2], data[3], !data[4].equals("0"), !data[5].equals("0"), data[6],
                    cemetery,
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
//        Person person = people.get("46266354792");
//        System.out.println(person.getBirthDate());
//
//        System.out.println(person);
//        System.out.println("Spouse: " + person.getSpouse());
//        System.out.println("Father: " + person.getFather());
//        System.out.println("Mother: " + person.getMother());
//        System.out.println("Children: " + person.getChildren());
//        System.out.println("-------------------------------------------");
//        searchRelativesRecursive(4, person);

    }

    public void setSelectedPerson(Person person) {
        this.selectedPerson = person;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public List<Person> searchPeopleByFilter(Person person) {

        List<Person> result = new ArrayList<>();
        if (person != null) {
            for (Person p : people.values()) {
                if (person.getName() != null && person.getName().equals(p.getName())
                        && person.getSurname() != null && person.getSurname().equals(p.getSurname())
                        && person.getBirthDate() != null && person.getBirthDate().equals(p.getBirthDate())
                        && person.getDeathDate() != null && person.getDeathDate().equals(p.getDeathDate())
                        && person.getId() != null && person.getId().equals(p.getId())
                        && person.getSex() != null && person.getSex().equals(p.getSex())
                        && person.getSpouseId() != null && person.getSpouseId().equals(p.getSpouseId())
                        && person.getFatherId() != null && person.getFatherId().equals(p.getFatherId())
                        && person.getMotherId() != null && person.getMotherId().equals(p.getMotherId())
                        && person.isDead() == p.isDead() //??? ölü mü aratabiliyoruz sadece ki bilemedim
                        && person.getCemetery() != null && person.getCemetery().equals(p.getCemetery())
                        && person.getDeathCause() != null && person.getDeathCause().equals(p.getDeathCause())
                ) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public List<Person> searchPeopleByDate(Date startDate, Date endDate) {

        List<Person> result = new ArrayList<>();
        if (startDate != null && endDate != null) {
            for (Person person : people.values()) {
                if (person.getDeathDate() != null && person.getDeathDate().after(startDate) && person.getDeathDate().before(endDate)) {
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

    public List<Person> searchRelativesRecursive(int generationInterval, Person person) {
        List<Person> result = new ArrayList<>();
        Stack<Person> childrenStack = new Stack<>();
        Stack<Person> ancestorsStack = new Stack<>();
        searchRelativesAncestors(generationInterval, person, ancestorsStack, result, "", person);
        searchRelativesChildren(generationInterval, person, childrenStack, result, "", person);
        return result;
    }

    public void searchRelativesAncestors(int generationInterval, Person person, Stack<Person> ancestorsStack, List<Person> result, String relationship, Person startPerson) {
        //TODO İnstert the results into the result list with correct order
        if (generationInterval == 0) {
            return;
        } else if (generationInterval > 0) {
            if (person.getMother() != null) {
                Person mother = person.getMother();
                System.out.println(startPerson.getName() + " " + startPerson.getSurname() + "'s " + relationship + " mother: " + mother.getName() + " " + mother.getSurname() + "(" + person.getName() + " " + person.getSurname() + "'s mother)");
                ancestorsStack.push(mother);
                searchRelativesAncestors(generationInterval - 1, mother, ancestorsStack, result, relationship + " grand", startPerson);
                ancestorsStack.pop();
            }
            if (person.getFather() != null) {
                Person father = person.getFather();
                System.out.println(startPerson.getName() + " " + startPerson.getSurname() + "'s " + relationship + " father: " + father.getName() + " " + father.getSurname() + "(" + person.getName() + " " + person.getSurname() + "'s father)");
                ancestorsStack.push(father);
                searchRelativesAncestors(generationInterval - 1, father, ancestorsStack, result, relationship + " grand", startPerson);
                ancestorsStack.pop();
            }
        }
    }

    public void searchRelativesChildren(int generationInterval, Person person, Stack<Person> childrenStack, List<Person> result, String relationship, Person startPerson) {
        //TODO İnstert the results into the result list with correct order
        if (generationInterval == 0) {
            return;
        } else if (generationInterval > 0) {
            for (Person child : person.getChildren()) {
                System.out.println(startPerson.getName() + " " + startPerson.getSurname() + "'s " + relationship + " child: " + child.getName() + " " + child.getSurname() + "(" + person.getName() + " " + person.getSurname() + "'s child)");
                childrenStack.push(child);
                searchRelativesChildren(generationInterval - 1, child, childrenStack, result, relationship + " grand", startPerson);
                childrenStack.pop();
            }
        }
    }

    public void consoleMode() {
        Scanner scanner = new Scanner(System.in);
        ConsoleReader reader = new ConsoleReader(scanner);

        String command = "";
        while (!command.matches("quit|exit")) {
            try {
                if (selectedPerson == null) {
                    ConsoleReader.Question loginQuestion = new ConsoleReader.Question("Login with ID", Person.QUESTIONS.getFirst().regex(), Person.QUESTIONS.getFirst().errorMessage(), true);
                    String id;
                    for (id = reader.getAnswer(loginQuestion); !people.containsKey(id); id = reader.getAnswer(loginQuestion))
                        System.out.println("Person with ID " + id + " not found.");
                    selectedPerson = people.get(id);
                    System.out.println("Successfully logged in as " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");
                } else if (command.equalsIgnoreCase("help")) {
                    //TODO: List commands
                    System.out.println("Help ");
                } else if (command.equalsIgnoreCase("logout")) {
                    selectedPerson = null;
                    System.out.println("Successfully logged out.");
                    continue;
                } else if (command.equalsIgnoreCase("add person")) {
                    Person newPerson = new Person(scanner, people, cemeteries);
                    people.put(newPerson.getId(), newPerson);
                    System.out.println("Successfully added person with ID " + newPerson.getId() + ".");
                } else if (command.matches("(?i)^remove\\sperson\\s(?!\\s).+$")) {
                    String id = command.split(" ")[2];
                    Person personToRemove = people.get(id);

                    if (personToRemove != null) {
                        personToRemove.remove();
                        people.remove(id);
                        System.out.println("Successfully removed person with ID " + id + ".");
                    } else {
                        System.out.println("Person with ID " + id + " not found.");
                    }

                    if (personToRemove == selectedPerson) {
                        selectedPerson = null;
                        System.out.println("Successfully logged out.");
                        continue;
                    }
                } else if (command.equalsIgnoreCase("add cemetery")) {
                    Cemetery newCemetery = new Cemetery(scanner, cemeteries);
                    cemeteries.put(newCemetery.getId(), newCemetery);
                    System.out.println("Successfully added cemetery with ID " + newCemetery.getId() + ".");
                } else if (command.matches("(?i)^remove\\scemetery\\s(?!\\s).+$")) {
                    String id = command.split(" ")[2];
                    Cemetery cemeteryToRemove = cemeteries.get(id);

                    if (cemeteryToRemove != null) {
                        String answer = reader.getAnswer(ConsoleReader.yesNo(
                                "There are " + cemeteryToRemove.getCount() + " people in this cemetery. Confirm removal?"));

                        if (answer.matches(ConsoleReader.YES_REGEX)) {
                            cemeteries.remove(id);
                            for (Person person : people.values()) {
                                person.removeCemeteryIfId(id);
                            }
                            System.out.println("Successfully removed cemetery with ID " + id + ".");
                        } else {
                            throw new CancellationException();
                        }
                    } else {
                        System.out.println("Cemetery with ID " + id + " not found.");
                    }
                } else if (!command.isBlank()) {
                    System.out.println("Invalid command. Type \"help\" for a list of commands.");
                }
            } catch (CancellationException e) {
                if (selectedPerson == null)
                    break;
                System.out.println("Cancelled operation.");
            }
            System.out.print("> ");

            command = scanner.nextLine();
        }
    }
}
