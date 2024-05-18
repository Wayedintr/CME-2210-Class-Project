import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;

public class Cemetree {
    Map<String, Person> people;

    Map<String, Cemetery> cemeteries;
    private Person selectedPerson;

    private record PersonRelationship(Person person, String relationship) {
    }

    private record ConsoleCommand(String command, String description, String[] args) {

        @Override
        public String toString() {
            return toString(false);
        }

        public String toString(boolean useArgs) {
            if (!useArgs || args.length == 0) {
                return String.format("%-20s %s", command.toUpperCase(Locale.ENGLISH), description);
            } else {
                StringBuilder argsString = new StringBuilder();
                for (String arg : args) {
                    argsString.append(arg).append("=<").append(arg.charAt(0)).append("> ");
                }
                return String.format("%-20s %s\n%20s %s", command.toUpperCase(Locale.ENGLISH), argsString, "", description);
            }
        }
    }

    private final String[] personFilter = {"id", "name", "surname", "sex", "birth_date", "death_date", "start_date", "end_date", "death_cause", "cemetery_id"};

    private final String[] cemeteryFilter = {"id", "name"};

    private final Map<String, ConsoleCommand> HELP = new LinkedHashMap<>() {{
        put("add person", new ConsoleCommand("add person", "Adds a new person", personFilter));
        put("remove person", new ConsoleCommand("remove person", "Removes a person", personFilter));
        put("search person", new ConsoleCommand("search person", "Searches for a person", personFilter));
        put("search relatives", new ConsoleCommand("search relatives", "Searches for relatives", new String[]{"generation_interval"}));

        put("view", new ConsoleCommand("view", "View", new String[]{"number"}));

        put("visit person", new ConsoleCommand("visit person", "Visits a person", personFilter));
        put("get visitor list", new ConsoleCommand("get visitor list", "Gets the visitor list of a person", personFilter));

        put("add cemetery", new ConsoleCommand("add cemetery", "Adds a new cemetery", new String[]{}));
        put("remove cemetery", new ConsoleCommand("remove cemetery", "Removes a cemetery", cemeteryFilter));

        put("help", new ConsoleCommand("help", "Shows help for <command>", new String[]{"command"}));
        put("logout", new ConsoleCommand("logout", "Logs out", new String[]{}));

        put("cancel", new ConsoleCommand("cancel", "Cancels current operation", new String[]{}));
        put("exit", new ConsoleCommand("exit", "Exits the program", new String[]{}));
    }};

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

            person.connect(people, cemeteries);
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
        return searchPeopleByFilter(filter, false);
    }

    public List<Person> searchPeopleByFilter(Person filter, boolean includeAlive) {
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
                        && (includeAlive || person.isDead())
                        && (filter.getCemetery() == null || filter.getCemetery().equals(person.getCemetery()))
                        && (filter.getDeathCause() == null || filter.getDeathCause().equals(person.getDeathCause()))
                ) {
                    result.add(person);
                }
            }
        }
        return result;
    }

    public List<Cemetery> searchCemeteriesByFilter(Cemetery filter) {
        List<Cemetery> result = new ArrayList<>();
        if (filter != null) {
            for (Cemetery cemetery : cemeteries.values()) {
                if ((filter.getId() == null || filter.getId().equals(cemetery.getId()))
                        && (filter.getName() == null || filter.getName().equals(cemetery.getName()))
                ) {
                    result.add(cemetery);
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
        return searchPeopleByCommand(command, false);
    }

    private List<Person> searchPeopleByCommand(String command, boolean includeAlive) {
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

        List<Person> result = searchPeopleByFilter(filter, includeAlive);
        if (startDate != null || endDate != null) {
            startDate = startDate == null ? new Date("01/01/1500") : startDate;
            endDate = endDate == null ? new Date() : endDate;
            result = searchPeopleByDate(result, startDate, endDate);
        }

        return result;
    }

    private Person selectPersonFromCommand(ConsoleReader reader, String command, int commandWordCount, List<Person> selectedPeople) {
        String[] args = command.split(" ");

        boolean contains = command.contains("=");

        int index = -1;
        if (selectedPeople != null && !contains) {
            try {
                index = Integer.parseInt(args[commandWordCount]) - 1;
                if (index < 0 || index >= selectedPeople.size()) {
                    System.out.println("Please enter a number between 1 and " + selectedPeople.size() + ".");
                }
            } catch (NumberFormatException ignored) {
            }
        } else if ((selectedPeople == null || selectedPeople.isEmpty()) && !contains) {
            System.out.println("Can not pick a person.");
        }

        Person foundPerson = null;

        if (index == -1 && contains) {
            List<Person> peopleToSearch = searchPeopleByCommand(command);
            foundPerson = selectPersonInList(reader, peopleToSearch);
        } else if (selectedPeople != null && index != -1) {
            foundPerson = selectedPeople.get(index);
        }

        return foundPerson;
    }

    private Person selectPersonInList(ConsoleReader reader, List<Person> list) {
        Person foundPerson = null;

        if (list.size() > 1) {
            System.out.println("Found " + list.size() + " people. Please select one:");
            System.out.println(Person.toRowHeader());
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i).toRowString(i + 1));
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

    private List<Cemetery> searchCemeteriesByCommand(String command) {
        Map<String, String> argsMap = ConsoleReader.parseArguments(command);
        String id = argsMap.get("id");
        String name = argsMap.get("name");

        Cemetery filter = new Cemetery(id, name);

        return searchCemeteriesByFilter(filter);
    }

    private Cemetery selectCemeteryInList(ConsoleReader reader, List<Cemetery> list) {
        Cemetery foundCemetery = null;

        if (list.size() > 1) {
            System.out.println("Found " + list.size() + " cemeteries. Please select one:");
            for (int i = 0; i < list.size(); i++) {
                Cemetery cemetery = list.get(i);
                System.out.printf("%d - %s\n", (i + 1), cemetery.getName());
            }
            int answer;
            for (answer = Integer.parseInt(reader.getAnswer(ConsoleReader.QUESTION_INT));
                 answer < 1 || answer > list.size();
                 answer = Integer.parseInt(reader.getAnswer(ConsoleReader.QUESTION_INT))) {
                System.out.println("Invalid input. Please enter a number between 1 and " + list.size() + ".");
            }
            foundCemetery = list.get(answer - 1);
        } else if (list.size() == 1) {
            foundCemetery = list.get(0);
        } else {
            System.out.println("Cemetery not found.");
        }

        return foundCemetery;
    }

    public void consoleMode() {
        Scanner scanner = new Scanner(System.in);
        ConsoleReader reader = new ConsoleReader(scanner);

        String command = "";
        List<Person> selectedPeople = null;

        selectedPerson = people.get("12312342353");
        while (!command.matches("(?i)^quit|exit$")) {
            try {
                if (selectedPerson != null) {
                    System.out.print("> ");
                    command = scanner.nextLine().trim().replaceAll("\\s+", " ");
                }

                if (selectedPerson == null) {
                    ConsoleReader.Question loginQuestion = new ConsoleReader.Question("Login with ID", Person.QUESTIONS.get(0).regex(), Person.QUESTIONS.get(0).errorMessage(), true);
                    String id;
                    for (id = reader.getAnswer(loginQuestion); !people.containsKey(id); id = reader.getAnswer(loginQuestion)) {
                        System.out.println("Person with ID " + id + " not found.");
                    }
                    selectedPerson = people.get(id);
                    if (selectedPerson.isDead()) {
                        System.out.println("Person with ID " + id + " is dead.");
                        selectedPerson = null;
                        continue;
                    }
                    System.out.println("Successfully logged in as " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");
                } else if (command.matches("(?i)^help .*$")) {
                    String[] args = command.split(" ");

                    if (args.length < 2) {
                        System.out.println("Use help <command> for more information on a specific command.");
                        continue;
                    }

                    ConsoleCommand helpCommand = HELP.get(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase(Locale.ENGLISH));
                    if (helpCommand != null) {
                        System.out.println(helpCommand.toString(true));
                    } else {
                        System.out.println("Command not found.");
                    }

                } else if (command.equalsIgnoreCase("help")) {
                    System.out.println("Use help <command> for more information on a specific command.");
                    for (Map.Entry<String, ConsoleCommand> commandToHelp : HELP.entrySet()) {
                        System.out.println(commandToHelp.getValue());
                    }
                } else if (command.equalsIgnoreCase("logout")) {
                    selectedPerson = null;
                    System.out.println("Successfully logged out.");
                } else if (command.equalsIgnoreCase("add person")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to add people.");
                        continue;
                    }

                    Person newPerson = new Person(scanner, people, cemeteries);
                    people.put(newPerson.getId(), newPerson);
                    newPerson.connect(people, cemeteries);

                    System.out.println("Successfully added " + newPerson.getFullName() + ".");
                } else if (command.matches("(?i)^remove person.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to remove people.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person personToRemove = selectPersonFromCommand(reader, command, 2, selectedPeople);

                    if (personToRemove != null) {
                        personToRemove.remove();
                        people.remove(personToRemove.getId());
                        System.out.println("Successfully removed " + personToRemove.getFullName() + ".");
                    } else {
                        System.out.println("Person not found.");
                    }

                    if (personToRemove == selectedPerson) {
                        selectedPerson = null;
                        System.out.println("Successfully logged out.");
                    }
                } else if (command.matches("(?i)^search person.*$")) {
                    if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    selectedPeople = searchPeopleByCommand(command);
                    System.out.println("Found " + selectedPeople.size() + " people. Use view <number> to view details.");

                    System.out.println(Person.toRowHeader());
                    for (int i = 0; i < selectedPeople.size(); i++) {
                        System.out.println(selectedPeople.get(i).toRowString(i + 1));
                    }
                } else if (command.matches("(?i)^view.*$")) {
                    if (command.split(" ").length < 2) {
                        System.out.println("Please enter a number.");
                        continue;
                    }

                    Person personToView = selectPersonFromCommand(reader, command, 1, selectedPeople);

                    if (personToView != null) {
                        System.out.println(personToView.details(selectedPerson.isAdmin()));
                    }
                } else if (command.matches("(?i)^visit person.*$")) {
                    if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person foundPerson = selectPersonFromCommand(reader, command, 2, selectedPeople);

                    if (foundPerson != null && foundPerson.isDead()) {
                        if (foundPerson.getCemetery() == null) {
                            System.out.println("Can not find the cemetery of " + foundPerson.getName() + " " + foundPerson.getSurname() + ".");
                            continue;
                        }
                        foundPerson.getCemetery().addVisitor(foundPerson, selectedPerson, new Date());
                        System.out.println("Successfully visited " + foundPerson.getName() + " " + foundPerson.getSurname() + " in " + foundPerson.getCemetery().getName() + " by " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");
                    } else {
                        System.out.println("Can not find person to visit.");
                    }
                } else if (command.matches("(?i)^get visitor list.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to get visitor list.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person foundPerson = selectPersonFromCommand(reader, command, 3, selectedPeople);

                    if (foundPerson != null) {
                        SortedSet<Cemetery.Visit> visitorList = foundPerson.getCemetery().getVisitorsOfPerson(foundPerson);

                        if (visitorList == null) {
                            System.out.println("There are no records of visitors of " + foundPerson.getFullName() + " in " + foundPerson.getCemetery().getName() + ".");
                            continue;
                        }

                        System.out.println("Visitor list of " + foundPerson.getFullName() + " in " + foundPerson.getCemetery().getName() + ":");

                        for (Cemetery.Visit visit : visitorList) {
                            System.out.println(visit.toString());
                        }
                    }
                } else if (command.equalsIgnoreCase("add cemetery")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to add cemeteries.");
                        continue;
                    }

                    Cemetery newCemetery = new Cemetery(scanner, cemeteries);
                    cemeteries.put(newCemetery.getId(), newCemetery);
                    newCemetery.connect(people);
                    System.out.println("Successfully added cemetery \"" + newCemetery.getName() + "\".");
                } else if (command.matches("(?i)^remove cemetery .*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to remove cemeteries.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter cemetery ID.");
                        continue;
                    }

                    List<Cemetery> cemeteriesToRemove = searchCemeteriesByCommand(command);
                    Cemetery cemeteryToRemove = selectCemeteryInList(reader, cemeteriesToRemove);

                    if (cemeteryToRemove != null) {
                        String answer = reader.getAnswer(ConsoleReader.yesNo(
                                "There are " + cemeteryToRemove.getCount() + " people in this cemetery. Confirm removal?"));

                        if (answer.matches(ConsoleReader.YES_REGEX)) {
                            cemeteries.remove(cemeteryToRemove.getId());
                            for (Person person : people.values()) {
                                person.removeCemeteryIfId(cemeteryToRemove.getId());
                            }
                            System.out.println("Successfully removed cemetery with ID " + cemeteryToRemove.getId() + ".");
                        } else {
                            throw new CancellationException();
                        }
                    }
                } else if (command.matches("(?i)^search relatives.*$")) {
                    String[] args = command.split(" ");

                    int generationInterval = 2;
                    if (args.length < 3) {
                        System.out.println("Using default generation interval of " + generationInterval + ".");
                    }

                    if (args.length >= 3) {
                        try {
                            generationInterval = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid generation interval. Please enter a number.");
                            continue;
                        }
                    }


                    List<PersonRelationship> result = searchRelativesRecursive(generationInterval, selectedPerson);

                    if (result.isEmpty()) {
                        System.out.println("No relatives found.");
                    } else {
                        System.out.println("Found " + result.size() + " relatives. Use view <number> to view details.");
                        selectedPeople = new ArrayList<>(result.size());
                        for (int i = 0; i < result.size(); i++) {
                            PersonRelationship personRelationship = result.get(i);
                            selectedPeople.add(personRelationship.person);
                            System.out.printf("%-3d %-12s %-12s %s\n", i + 1, personRelationship.person.getName(), personRelationship.person.getSurname(), personRelationship.relationship);
                        }
                    }
                } else if (!command.isBlank() && !command.matches("(?i)^quit|exit$")) {
                    System.out.println("Invalid command. Type \"help\" for a list of commands.");
                }
            } catch (CancellationException e) {
                if (selectedPerson == null)
                    break;
                System.out.println("Cancelled operation.");
            }
        }
    }
}
