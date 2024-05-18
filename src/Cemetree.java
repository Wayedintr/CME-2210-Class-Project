import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;

public class Cemetree {
    Map<String, Person> people;

    Map<String, Cemetery> cemeteries;
    private Person selectedPerson;

    record PersonRelationship(Person person, String relationship) {
    }

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

    public void saveToFile(String fileName) throws IOException {
        // Save cemeteries
        BufferedWriter cemeteryWriter = new BufferedWriter(new FileWriter(fileName + "_cemeteries.csv"));

        cemeteryWriter.write(Cemetery.toCsvHeader() + "\n");
        for (Cemetery cemetery : cemeteries.values())
            cemeteryWriter.write(cemetery.toCsvString() + "\n");
        cemeteryWriter.close();

        // Save people
        BufferedWriter peopleWriter = new BufferedWriter(new FileWriter(fileName + "_people.csv"));

        // Sort people by birthdate
        List<Person> peopleList = new ArrayList<>(people.values());
        peopleList.sort(Person::compareTo);

        peopleWriter.write(Person.toCsvHeader() + "\n");
        for (Person person : peopleList) {
            peopleWriter.write(person.toCsvString() + "\n");
        }
        peopleWriter.close();

        // Save visitors
        BufferedWriter visitorWriter = new BufferedWriter(new FileWriter(fileName + "_visitors.csv"));

        visitorWriter.write("cemeteryId,visitedId,visitorId,time\n");
        for (Cemetery cemetery : cemeteries.values()) {
            Map<Person, SortedSet<Cemetery.Visit>> visitorList = cemetery.getVisitorList();

            for (Map.Entry<Person, SortedSet<Cemetery.Visit>> entry : visitorList.entrySet()) {
                for (Cemetery.Visit visit : entry.getValue()) {
                    visitorWriter.write(String.format("%s,%s,%s,%s",
                            cemetery.getId(),
                            entry.getKey().getId(),
                            visit.getPerson().getId(),
                            visit.getDate().toStringCLK()
                    ));
                    visitorWriter.write("\n");
                }
            }
        }
        visitorWriter.close();
    }

    public void loadFromFile(String fileName) throws IOException {
        String line;

        // Load cemeteries
        BufferedReader cemeteryReader = new BufferedReader(new FileReader(fileName + "_cemeteries.csv"));

        // skip header
        cemeteryReader.readLine();

        line = cemeteryReader.readLine();
        while (line != null) {
            String[] data = line.split(",");
            Address address = new Address(data[2], data[3], data[4], data[5], data[6], Double.parseDouble(data[7]), Double.parseDouble(data[8]));
            Cemetery cemetery = new Cemetery(data[0], data[1], address);
            cemeteries.put(cemetery.getId(), cemetery);
            line = cemeteryReader.readLine();
        }
        cemeteryReader.close();

        // Load people
        BufferedReader peopleReader = new BufferedReader(new FileReader(fileName + "_people.csv"));

        // skip header
        peopleReader.readLine();

        line = peopleReader.readLine();
        while (line != null) {
            String[] data = line.split(",");
            Cemetery cemetery = cemeteries.get(data[7]);


            Person person = new Person(data[0], data[1], data[2], data[3], !data[4].equals("0"), !data[5].equals("0"), data[6],
                    cemetery,
                    !data[8].isBlank() ? new Date(data[8]) : null,
                    !data[9].isBlank() ? new Date(data[9]) : null,
                    data[10].isBlank() ? null : data[10],
                    data[11].isBlank() ? null : data[11],
                    data.length == 13 ? data[12] : null
            );

            if (person.hasMotherId() && people.get(person.getMotherId()) != null) {
                Person mother = people.get(person.getMotherId());
                person.setMother(mother);
                mother.addChild(person);
            }
            if (person.hasFatherId() && people.get(person.getFatherId()) != null) {
                Person father = people.get(person.getFatherId());
                person.setFather(father);
                father.addChild(person);
            }
            if (person.hasSpouseId() && people.get(person.getSpouseId()) != null) {
                Person spouse = people.get(person.getSpouseId());
                if (spouse != null) {
                    person.setSpouse(spouse);
                    spouse.setSpouse(person);
                }
            }
            if (person.getCemetery() != null && person.isDead()) {
                if (cemetery.count >= cemetery.CAPACITY) {
                    System.out.println("Cemetery " + cemetery.getId() + " is full. Person " + person.getName() + " " + person.getSurname() + " cannot be added.");
                } else {
                    people.put(person.getId(), person);
                }
                cemetery.incrementCount();
            } else if (person.getCemetery() == null && !person.isDead()) {
                people.put(person.getId(), person);
            }
            line = peopleReader.readLine();
        }
        peopleReader.close();

        // Load visitors
        BufferedReader visitorReader = new BufferedReader(new FileReader(fileName + "_visitors.csv"));

        // Skip header
        visitorReader.readLine();

        line = visitorReader.readLine();
        while (line != null) {
            String[] data = line.split(",");
            Cemetery cemetery = cemeteries.get(data[0]);
            Person visitedPerson = people.get(data[1]);
            Person visitorPerson = people.get(data[2]);
            Date time = new Date(data[3], true);

            if (cemetery != null && visitedPerson != null && visitorPerson != null) {
                cemetery.addVisitor(visitedPerson, visitorPerson, time);
            }

            line = visitorReader.readLine();
        }
        visitorReader.close();
    }

    public void setSelectedPerson(Person person) {
        this.selectedPerson = person;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public List<Person> searchPeopleByFilter(Person filter) {
        List<Person> result = new ArrayList<>();

        if (filter != null) {
            for (Person person : people.values()) {
                if ((filter.getName() == null || filter.getName().equalsIgnoreCase(person.getName()))
                        && (filter.getSurname() == null || filter.getSurname().equalsIgnoreCase(person.getSurname()))
                        && (filter.getBirthDate() == null || filter.getBirthDate().equals(person.getBirthDate()))
                        && (filter.getDeathDate() == null || filter.getDeathDate().equals(person.getDeathDate()))
                        && (filter.getId() == null || filter.getId().equals(person.getId()))
                        && (filter.getSex() == null || filter.getSex().equalsIgnoreCase(person.getSex()))
                        && (filter.getSpouseId() == null || filter.getSpouseId().equals(person.getSpouseId()))
                        && (filter.getFatherId() == null || filter.getFatherId().equals(person.getFatherId()))
                        && (filter.getMotherId() == null || filter.getMotherId().equals(person.getMotherId()))
                        && person.isDead()
                        && (filter.getCemetery() == null || filter.getCemetery().equals(person.getCemetery()))
                        && (filter.getDeathCause() == null || filter.getDeathCause().equals(person.getDeathCause()))
                ) {
                    result.add(person);
                }
            }
        }
        return result;
    }

    public List<Person> searchPeopleByDate(List<Person> searchList, Date startDate, Date endDate) {

        List<Person> result = new ArrayList<>();
        if (startDate != null && endDate != null) {
            for (Person person : searchList) {
                if (person.getDeathDate() != null && person.getDeathDate().after(startDate) && person.getDeathDate().before(endDate)) {
                    result.add(person);
                }
            }
        }
        return result;
    }

    public List<PersonRelationship> searchRelativesRecursive(int generationInterval, Person person) {
        List<PersonRelationship> result = new ArrayList<>();
        result.add(new PersonRelationship(person, " Searching Relatives..."));
        Stack<Person> childrenStack = new Stack<>();
        Stack<Person> ancestorsStack = new Stack<>();
        searchRelativesAncestors(generationInterval, person, ancestorsStack, result, "", person);
        searchRelativesChildren(generationInterval, person, childrenStack, result, "", person);
        return result;
    }

    public void searchRelativesAncestors(int generationInterval, Person person, Stack<Person> ancestorsStack, List<PersonRelationship> result, String relationship, Person startPerson) {
        if (generationInterval > 0) {
            if (person.getMother() != null) {
                Person mother = person.getMother();
                PersonRelationship personRelationship = new PersonRelationship(person, "");
                PersonRelationship p = new PersonRelationship(mother, relationship + " Mother (" + person.getName() + " " + person.getSurname() + "' s Mother)");
                result.add(p);
                ancestorsStack.push(mother);
                searchRelativesAncestors(generationInterval - 1, mother, ancestorsStack, result, relationship + " Grand", startPerson);
                ancestorsStack.pop();
            }
            if (person.getFather() != null) {
                Person father = person.getFather();
                PersonRelationship p = new PersonRelationship(father, relationship + " Father (" + person.getName() + " " + person.getSurname() + "' s Father)");
                result.add(p);
                ancestorsStack.push(father);
                searchRelativesAncestors(generationInterval - 1, father, ancestorsStack, result, relationship + " Grand", startPerson);
                ancestorsStack.pop();
            }
        }
    }


    public void searchRelativesChildren(int generationInterval, Person person, Stack<Person> childrenStack, List<PersonRelationship> result, String relationship, Person startPerson) {
        if (generationInterval > 0) {
            for (Person child : person.getChildren()) {
                childrenStack.push(child);
                PersonRelationship p = new PersonRelationship(child, relationship + " Child (" + person.getName() + " " + person.getSurname() + "' s Child)");
                result.add(p);
                searchRelativesChildren(generationInterval - 1, child, childrenStack, result, relationship + " Grand", startPerson);
                childrenStack.pop();
            }
        }
    }

    private List<Person> searchPeopleByCommand(String command) {
        Map<String, String> argsMap = ConsoleReader.parseArguments(command);
        Cemetery cemetery = cemeteries.get(argsMap.get("cemetery_id"));
        if (argsMap.containsKey("cemetery_id") && cemetery == null) {
            cemetery = new Cemetery();
        }

        String birthDateStr = argsMap.get("birth_date");
        String deathDateStr = argsMap.get("death_date");
        String startDateStr = argsMap.get("start_date");
        String endDateStr = argsMap.get("end_date");

        Date birthDate, deathDate, startDate, endDate;

        try {
            birthDate = birthDateStr == null ? null : new Date(birthDateStr);
            deathDate = deathDateStr == null ? null : new Date(deathDateStr);
            startDate = startDateStr == null ? null : new Date(startDateStr);
            endDate = endDateStr == null ? null : new Date(endDateStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Incorrect date format");
            throw new CancellationException();
        }

        Person filter = new Person(argsMap.get("id"), argsMap.get("name"), argsMap.get("surname"), argsMap.get("sex"), argsMap.get("death_cause"), cemetery, birthDate, deathDate);

        List<Person> result = searchPeopleByFilter(filter);
        if (startDate != null || endDate != null) {
            startDate = startDate == null ? new Date("01/01/1500") : startDate;
            endDate = endDate == null ? new Date() : endDate;
            result = searchPeopleByDate(result, startDate, endDate);
        }

        return result;
    }

    private Person selectPersonInList(ConsoleReader reader, List<Person> list) {
        Person foundPerson = null;

        if (list.size() > 1) {
            System.out.println("Found " + list.size() + " people. Please select one:");
            for (int i = 0; i < list.size(); i++) {
                Person person = list.get(i);
                System.out.println((i + 1) + "- " + person.getName() + " " + person.getSurname() + ", " + person.getBirthDate() + " - " + person.getDeathDate());
            }
            int answer;
            for (answer = Integer.parseInt(reader.getAnswer(ConsoleReader.QUESTION_INT));
                 answer < 1 || answer > list.size();
                 answer = Integer.parseInt(reader.getAnswer(ConsoleReader.QUESTION_INT))) {
                System.out.println("Invalid input. Please enter a number between 1 and " + list.size() + ".");
            }
            foundPerson = list.get(answer - 1);
        } else if (list.size() == 1) {
            foundPerson = list.get(0);
        } else {
            System.out.println("Person not found.");
        }

        return foundPerson;
    }

    public void consoleMode() {
        Map<String, String> help = new LinkedHashMap<>();
        help.put("help", "List commands");
        help.put("logout", "Log out");
        help.put("cancel", "Cancel current operation");
        help.put("exit", "Exit the program");

        Scanner scanner = new Scanner(System.in);
        ConsoleReader reader = new ConsoleReader(scanner);

        String command = "";
        List<Person> selectedPeople;

        selectedPerson = people.get("84282308566");
        while (!command.matches("(?i)^quit|exit$")) {
            try {
                if (selectedPerson == null) {
                    ConsoleReader.Question loginQuestion = new ConsoleReader.Question("Login with ID", Person.QUESTIONS.get(0).regex(), Person.QUESTIONS.get(0).errorMessage(), true);
                    String id;
                    for (id = reader.getAnswer(loginQuestion); !people.containsKey(id); id = reader.getAnswer(loginQuestion))
                        System.out.println("Person with ID " + id + " not found.");
                    selectedPerson = people.get(id);
                    System.out.println("Successfully logged in as " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");
                } else if (command.equalsIgnoreCase("help")) {
                    System.out.println("Use -h for help with a command.");
                    for (Map.Entry<String, String> entry : help.entrySet()) {
                        System.out.printf("%-20s %s%n", entry.getKey().toUpperCase(Locale.ENGLISH), entry.getValue());
                    }
                } else if (command.equalsIgnoreCase("logout")) {
                    selectedPerson = null;
                    System.out.println("Successfully logged out.");
                    continue;
                } else if (command.equalsIgnoreCase("add person")) {
                    Person newPerson = new Person(scanner, people, cemeteries);
                    people.put(newPerson.getId(), newPerson);
                    System.out.println("Successfully added person with ID " + newPerson.getId() + ".");
                } else if (command.matches("(?i)^remove person.*$")) {
                    String[] args = command.split(" ");

                    if (args.length < 3) {
                        System.out.println("Please enter person ID.");
                        continue;
                    }

                    String id = args[2];
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
                } else if (command.matches("(?i)^search person.*$")) {
                    String[] args = command.split(" ");

                    if (args.length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    selectedPeople = searchPeopleByCommand(command);
                    System.out.println("Found " + selectedPeople.size() + " people.");

                    for (int i = 0; i < selectedPeople.size(); i++) {
                        Person person = selectedPeople.get(i);
                        System.out.println((i + 1) + "- " + person.getName() + " " + person.getSurname());
                    }
                } else if (command.matches("(?i)^visit person.*$")) {
                    String[] args = command.split(" ");

                    if (args.length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    selectedPeople = searchPeopleByCommand(command);
                    Person foundPerson = selectPersonInList(reader, selectedPeople);

                    if (foundPerson != null && foundPerson.isDead()) {
                        foundPerson.getCemetery().addVisitor(foundPerson, selectedPerson, new Date());
                        System.out.println("Successfully visited " + foundPerson.getName() + " " + foundPerson.getSurname() + " in " + foundPerson.getCemetery().getName() + " by " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");
                    } else {
                        System.out.println("Can not find person to visit.");
                    }
                    //get visitor list by filter command
                } else if (command.matches("(?i)^get visitor list.*$")) {
                    String[] args = command.split(" ");

                    if (args.length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    selectedPeople = searchPeopleByCommand(command);
                    Person foundPerson = selectPersonInList(reader, selectedPeople);

                    if (foundPerson != null) {
                        SortedSet<Cemetery.Visit> visitorList = foundPerson.getCemetery().getVisitorsOfPerson(foundPerson);

                        System.out.println("Visitor list of " + foundPerson.getName() + " " + foundPerson.getSurname() + " in " + foundPerson.getCemetery().getName() + ":");

                        for (Cemetery.Visit visit : visitorList) {
                            System.out.println(visit.toString());
                        }
                    }
                } else if (command.equalsIgnoreCase("add cemetery")) {
                    Cemetery newCemetery = new Cemetery(scanner, cemeteries);
                    cemeteries.put(newCemetery.getId(), newCemetery);
                    System.out.println("Successfully added cemetery with ID " + newCemetery.getId() + ".");
                } else if (command.matches("(?i)^remove cemetery .*$")) {
                    String[] args = command.split(" ");

                    if (args.length < 3) {
                        System.out.println("Please enter cemetery ID.");
                        continue;
                    }

                    String id = args[2];
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

            command = scanner.nextLine().trim().replaceAll("\\s+", " ");
        }
    }
}
