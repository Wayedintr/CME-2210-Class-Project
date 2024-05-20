import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;

public class Cemetree {
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

    private final Map<String, Person> people = new HashMap<>();

    private final Map<String, Cemetery> cemeteries = new HashMap<>();

    private final String[] PERSON_FILTER = {"id", "name", "surname", "sex", "birth_date", "death_date", "start_date", "end_date", "death_cause", "cemetery_id", "sort_by"};

    private final String[] RELATIVE_FILTER = {"interval", "id", "name", "surname", "sex", "birth_date", "death_date", "start_date", "end_date", "death_cause", "cemetery_id", "sort_by"};

    private final String[] CEMETERY_FILTER = {"id", "name", "country", "city", "district", "neighbourhood", "street", "latitude", "longitude", "sort_by"};

    private final Map<String, ConsoleCommand> HELP = new LinkedHashMap<>(21) {{
        put("add person", new ConsoleCommand("add person", "Adds a new person", PERSON_FILTER));
        put("remove person", new ConsoleCommand("remove person", "Removes a person", PERSON_FILTER));
        put("edit person", new ConsoleCommand("edit person", "Edits a person", PERSON_FILTER));
        put("set dead", new ConsoleCommand("set dead", "Changes a person's status to dead", PERSON_FILTER));
        put("set alive", new ConsoleCommand("set alive", "Changes a person's status to alive", PERSON_FILTER));
        put("set admin", new ConsoleCommand("set admin", "Changes a person's admin status", PERSON_FILTER));
        put("set user", new ConsoleCommand("set user", "Changes a person's admin status", PERSON_FILTER));
        put("search person", new ConsoleCommand("search person", "Searches for a person (Use \"include alive\" flag to include alive people at search)", PERSON_FILTER));
        put("search relatives", new ConsoleCommand("search relatives", "Searches for relatives", RELATIVE_FILTER));

        put("add cemetery", new ConsoleCommand("add cemetery", "Adds a new cemetery", new String[]{}));
        put("remove cemetery", new ConsoleCommand("remove cemetery", "Removes a cemetery", CEMETERY_FILTER));
        put("edit cemetery", new ConsoleCommand("edit cemetery", "Edits a cemetery", CEMETERY_FILTER));
        put("search cemetery", new ConsoleCommand("search cemetery", "Searches for a cemetery", CEMETERY_FILTER));
        put("show statistics", new ConsoleCommand("show statistics", "Shows statistics (Use \"group\" flag to show statistics of all selected cemeteries)", CEMETERY_FILTER));

        put("view", new ConsoleCommand("view", "Views details of a person or cemetery (Use \"view person\" or \"view cemetery\" to view corresponding details)", new String[]{"number"}));

        put("visit person", new ConsoleCommand("visit person", "Visits a person", PERSON_FILTER));
        put("get visitor list", new ConsoleCommand("get visitor list", "Gets the visitor list of a person", PERSON_FILTER));

        put("help", new ConsoleCommand("help", "Shows help for <command>", new String[]{"command"}));
        put("logout", new ConsoleCommand("logout", "Logs out", new String[]{}));

        put("cancel", new ConsoleCommand("cancel", "Cancels current operation", new String[]{}));
        put("exit", new ConsoleCommand("exit", "Exits the program", new String[]{}));
    }};

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

        if (!new File(fileName + "_cemeteries.csv").exists()) {
            System.out.println("File not found: " + fileName + "_cemeteries.csv");
            throw new IOException("File not found: " + fileName + "_cemeteries.csv");
        } else if (!new File(fileName + "_people.csv").exists()) {
            System.out.println("File not found: " + fileName + "_people.csv");
            throw new IOException("File not found: " + fileName + "_people.csv");
        } else if (!new File(fileName + "_visitors.csv").exists()) {
            System.out.println("File not found: " + fileName + "_visitors.csv");
            throw new IOException("File not found: " + fileName + "_visitors.csv");
        }


        // Load cemeteries
        BufferedReader cemeteryReader = new BufferedReader(new FileReader(fileName + "_cemeteries.csv"));
        cemeteryReader.readLine();

        while ((line = cemeteryReader.readLine()) != null) {
            String[] data = line.split(",", -1);
            Address address = new Address(data[2], data[3], data[4], data[5], data[6], Double.parseDouble(data[7]), Double.parseDouble(data[8]));
            Cemetery cemetery = new Cemetery(data[0], data[1], address);
            cemeteries.put(cemetery.getId(), cemetery);
        }
        cemeteryReader.close();

        // Load people
        BufferedReader peopleReader = new BufferedReader(new FileReader(fileName + "_people.csv"));
        peopleReader.readLine();

        while ((line = peopleReader.readLine()) != null) {
            String[] data = line.split(",", -1);
            Cemetery cemetery = cemeteries.get(data[7]);

            Person person = new Person(data[0], data[1], data[2], data[3], !data[4].equals("0"), !data[5].equals("0"), data[6],
                    cemetery,
                    !data[8].isBlank() ? new Date(data[8]) : null,
                    !data[9].isBlank() ? new Date(data[9]) : null,
                    data[10].isBlank() ? null : data[10],
                    data[11].isBlank() ? null : data[11],
                    data.length == 13 ? data[12] : null
            );

            // Connect person to parents, spouse, children and cemetery
            person.connect(people);
        }
        peopleReader.close();

        // Load visitors
        BufferedReader visitorReader = new BufferedReader(new FileReader(fileName + "_visitors.csv"));
        visitorReader.readLine();

        while ((line = visitorReader.readLine()) != null) {
            String[] data = line.split(",", -1);
            Cemetery cemetery = cemeteries.get(data[0]);
            Person visitedPerson = people.get(data[1]);
            Person visitorPerson = people.get(data[2]);
            Date time = new Date(data[3], true);

            if (cemetery != null && visitedPerson != null && visitorPerson != null) {
                cemetery.addVisitor(visitedPerson, visitorPerson, time);
            }
        }
        visitorReader.close();
    }

    public List<Person> searchPeopleByFilter(Person filter, boolean includeAlive) {
        List<Person> result = new ArrayList<>();

        if (filter != null) {
            for (Person person : people.values()) {
                if (person.matches(filter) && (includeAlive || person.isDead()))
                    result.add(person);
            }
        }
        return result;
    }

    public List<Cemetery> searchCemeteriesByFilter(Cemetery filter) {
        List<Cemetery> result = new ArrayList<>();
        if (filter != null) {
            for (Cemetery cemetery : cemeteries.values()) {
                if (cemetery.matches(filter))
                    result.add(cemetery);
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
        searchRelativesAncestors(generationInterval, person, ancestorsStack, result, "");
        searchRelativesChildren(generationInterval, person, childrenStack, result, "");
        return result;
    }

    public void searchRelativesAncestors(int generationInterval, Person person, Stack<Person> ancestorsStack, List<PersonRelationship> result, String relationship) {
        if (generationInterval > 0) {
            if (person.getMother() != null) {
                Person mother = person.getMother();
                PersonRelationship p = new PersonRelationship(mother, relationship + " Mother (" + person.getName() + " " + person.getSurname() + "' s Mother)");
                result.add(p);
                ancestorsStack.push(mother);
                searchRelativesAncestors(generationInterval - 1, mother, ancestorsStack, result, relationship + " Grand");
                ancestorsStack.pop();
            }
            if (person.getFather() != null) {
                Person father = person.getFather();
                PersonRelationship p = new PersonRelationship(father, relationship + " Father (" + person.getName() + " " + person.getSurname() + "' s Father)");
                result.add(p);
                ancestorsStack.push(father);
                searchRelativesAncestors(generationInterval - 1, father, ancestorsStack, result, relationship + " Grand");
                ancestorsStack.pop();
            }
        }
    }

    public void searchRelativesChildren(int generationInterval, Person person, Stack<Person> childrenStack, List<PersonRelationship> result, String relationship) {
        if (generationInterval > 0) {
            for (Person child : person.getChildren()) {
                childrenStack.push(child);
                PersonRelationship p = new PersonRelationship(child, relationship + " Child (" + person.getName() + " " + person.getSurname() + "' s Child)");
                result.add(p);
                searchRelativesChildren(generationInterval - 1, child, childrenStack, result, relationship + " Grand");
                childrenStack.pop();
            }
        }
    }

    public String showStatistics(Cemetery filter) {
        String result = "Statistics\n";

        Map<String, Integer> deathCauses = new LinkedHashMap<>();
        Map<Cemetery, Integer> deathCemeteries = new LinkedHashMap<>();

        double sumOfAges = 0;
        double maleCount = 0;
        double femaleCount = 0;
        double deathCount = 0;
        double deadCount = 0;

        for (Person person : people.values()) {
            if (person.dead && person.getCemetery() != null && (filter == null || person.getCemetery().matches(filter))) {
                sumOfAges += person.getAge();
                deadCount++;

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

                if (deathCemeteries.containsKey(person.getCemetery()))
                    deathCemeteries.put(person.getCemetery(), deathCemeteries.get(person.getCemetery()) + 1);
                else
                    deathCemeteries.put(person.getCemetery(), 1);
            }
        }

        deathCauses = Cemetery.sortByValue(deathCauses);
        deathCemeteries = Cemetery.sortByValue(deathCemeteries);

        result += String.format("%-28s : %.2f\n", "Average age", sumOfAges / deadCount);
        result += String.format("%-28s : %.2f%%/%.2f%%\n", "Male/Female", maleCount / deadCount * 100.0, femaleCount / deadCount * 100.0);

        result += "─".repeat(56) + "\n";

        result += String.format("%-28s : %.0f", "Total deaths", deathCount);
        for (Map.Entry<String, Integer> entry : deathCauses.entrySet()) {
            double percentage = entry.getValue() / deathCount * 100.0;
            if (percentage >= 1)
                result += String.format("\n%-28s : %2.0f%% %s", entry.getKey(), percentage, "▉".repeat((int) percentage));
        }

        result += "\n" + "─".repeat(56) + "\n";

        result += String.format("%-28s : %d", "Total cemetery count", deathCemeteries.size());
        for (Map.Entry<Cemetery, Integer> entry : deathCemeteries.entrySet()) {
            double percentage = entry.getValue() / deathCount * 100.0;
            if (percentage >= 1)
                result += String.format("\n%-28s : %2.0f%% %s", entry.getKey().getName(), percentage, "▉".repeat((int) percentage));
        }

        return result;
    }

    private List<Person> searchPeopleByCommand(String command, boolean includeAlive) {
        Map<String, String> arguments = ConsoleReader.parseArguments(command);
        Cemetery cemetery = null;
        if (arguments.containsKey("cemetery_id")) {
            List<Cemetery> cemeteries = searchCemeteriesByFilter(new Cemetery(arguments.get("cemetery_id"), null, null));
            cemetery = !cemeteries.isEmpty() ? cemeteries.get(0) : null;
        }

        String birthDateStr = arguments.get("birth_date");
        String deathDateStr = arguments.get("death_date");
        String startDateStr = arguments.get("start_date");
        String endDateStr = arguments.get("end_date");

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

        Person filter = new Person(arguments.get("id"), arguments.get("name"), arguments.get("surname"), arguments.get("sex"), arguments.get("death_cause"), cemetery, birthDate, deathDate);

        List<Person> result = searchPeopleByFilter(filter, includeAlive);
        if (startDate != null || endDate != null) {
            startDate = startDate == null ? new Date("01/01/1500") : startDate;
            endDate = endDate == null ? new Date() : endDate;
            result = searchPeopleByDate(result, startDate, endDate);
        }

        return result;
    }

    private Person selectPersonFromCommand(ConsoleReader reader, String command, int commandWordCount, List<Person> selectedPeople, boolean includeAlive) {
        String[] args = command.split(" ");

        boolean contains = command.contains("=");

        int index = -1;
        if (selectedPeople != null && !contains) {
            try {
                index = Integer.parseInt(args[commandWordCount]) - 1;
                if (index < 0 || index >= selectedPeople.size()) {
                    System.out.println("Please enter a number between 1 and " + selectedPeople.size() + ".");
                    return null;
                }
            } catch (NumberFormatException ignored) {
            }
        } else if ((selectedPeople == null || selectedPeople.isEmpty()) && !contains) {
            System.out.println("Can not pick a person.");
        }

        Person foundPerson = null;

        if (index == -1 && contains) {
            List<Person> peopleToSearch = searchPeopleByCommand(command, includeAlive);

            Map<String, String> argsMap = ConsoleReader.parseArguments(command);

            if (argsMap.containsKey("sort_by"))
                peopleToSearch.sort(Person.getComparator(argsMap.get("sort_by")));

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

        Cemetery filter = new Cemetery(id, name, new Address(argsMap.get("country"), argsMap.get("city"), argsMap.get("district"), argsMap.get("neighbourhood"), argsMap.get("street")));

        List<Cemetery> result = searchCemeteriesByFilter(filter);

        if (argsMap.containsKey("sort_by"))
            result.sort(Cemetery.getComparator(argsMap.get("sort_by")));

        return result;
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

    private Cemetery selectCemeteryFromCommand(ConsoleReader reader, String command, int commandWordCount, List<Cemetery> selectedCemeteries) {
        String[] args = command.split(" ");

        boolean contains = command.contains("=");

        int index = -1;
        if (selectedCemeteries != null && !contains) {
            try {
                index = Integer.parseInt(args[commandWordCount]) - 1;
                if (index < 0 || index >= selectedCemeteries.size()) {
                    System.out.println("Please enter a number between 1 and " + selectedCemeteries.size() + ".");
                    return null;
                }
            } catch (NumberFormatException ignored) {
            }
        } else if ((selectedCemeteries == null || selectedCemeteries.isEmpty()) && !contains) {
            System.out.println("Can not pick a cemetery.");
        }

        Cemetery foundCemetery = null;

        if (index == -1 && contains) {
            List<Cemetery> cemeteriesToSearch = searchCemeteriesByCommand(command);
            foundCemetery = selectCemeteryInList(reader, cemeteriesToSearch);
        } else if (selectedCemeteries != null && index != -1) {
            foundCemetery = selectedCemeteries.get(index);
        }

        return foundCemetery;
    }

    public void consoleMode() {
        Person selectedPerson = null;

        Scanner scanner = new Scanner(System.in);
        ConsoleReader reader = new ConsoleReader(scanner);

        String command = "";
        List<Person> selectedPeople = null;
        List<Cemetery> selectedCemeteries = null;

        enum ViewMode {
            PERSONS,
            CEMETERIES
        }
        ViewMode viewMode = ViewMode.PERSONS;

        System.out.println("Welcome to Cemetree. Type \"help\" for a list of commands.");
        System.out.println("Type \"register\" to register as a new person.");
        System.out.println("Type \"exit\" to exit.");

        while (!command.matches("(?i)^quit|exit$")) {
            try {
                if (selectedPerson != null) {
                    System.out.print("> ");
                    command = scanner.nextLine().trim().replaceAll("\\s+", " ");
                }

                // Login
                if (selectedPerson == null) {
                    ConsoleReader.Question loginQuestion = new ConsoleReader.Question("Login with ID", "^[\\p{L}\\p{M}'-(0-9)]{2,64}$", Person.QUESTIONS.get(0).errorMessage(), true);
                    String id;
                    for (id = reader.getAnswer(loginQuestion); !people.containsKey(id) && !id.equalsIgnoreCase("register"); id = reader.getAnswer(loginQuestion)) {
                        System.out.println("Person with ID " + id + " not found.");
                    }

                    if (id.equalsIgnoreCase("register")) {
                        selectedPerson = new Person(reader, people, cemeteries);
                        if (selectedPerson.isDead()) {
                            selectedPerson.setAlive();
                            System.out.println("You can not register as a dead person.\nSetting person as alive.");
                        }
                        people.put(selectedPerson.getId(), selectedPerson);
                        selectedPerson.connect(people);
                        System.out.println("Successfully registered as " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");

                        continue;
                    }

                    selectedPerson = people.get(id);
                    if (selectedPerson.isDead()) {
                        System.out.println("Person with ID " + id + " is dead.");
                        selectedPerson = null;
                        continue;
                    }
                    System.out.println("Successfully logged in as " + selectedPerson.getName() + " " + selectedPerson.getSurname() + (selectedPerson.isAdmin() ? " as admin." : "."));
                }

                // Help with specific command
                else if (command.matches("(?i)^help .*$")) {
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

                }

                // Help
                else if (command.equalsIgnoreCase("help")) {
                    System.out.println("Use help <command> for more information on a specific command.");
                    for (Map.Entry<String, ConsoleCommand> commandToHelp : HELP.entrySet()) {
                        System.out.println(commandToHelp.getValue());
                    }
                }

                // Logout
                else if (command.equalsIgnoreCase("logout")) {
                    selectedPerson = null;
                    System.out.println("Successfully logged out.");
                }

                // Add Person
                else if (command.equalsIgnoreCase("add person")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to add people.");
                        continue;
                    }

                    Person newPerson = new Person(reader, people, cemeteries);
                    people.put(newPerson.getId(), newPerson);
                    newPerson.connect(people);

                    System.out.println("Successfully added " + newPerson.getFullName() + ".");
                }

                // Remove Person
                else if (command.matches("(?i)^remove person.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to remove people.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person personToRemove = selectPersonFromCommand(reader, command, 2, selectedPeople, true);

                    if (personToRemove != null) {
                        personToRemove.removeConnections();
                        people.remove(personToRemove.getId());
                        System.out.println("Successfully removed " + personToRemove.getFullName() + ".");
                    }

                    if (personToRemove == selectedPerson) {
                        selectedPerson = null;
                        System.out.println("Successfully logged out.");
                    }
                }

                // Edit person
                else if (command.matches("(?i)^edit person.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to edit people.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person personToEdit = selectPersonFromCommand(reader, command, 2, selectedPeople, true);

                    if (personToEdit != null) {
                        personToEdit.edit(reader, people, cemeteries);
                        System.out.println("Successfully updated " + personToEdit.getFullName() + ".");
                    }
                }

                // Set person to dead
                else if (command.matches("(?i)^set dead.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to edit people.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person personToSetDead = selectPersonFromCommand(reader, command, 2, selectedPeople, true);

                    if (personToSetDead != null) {
                        if (personToSetDead == selectedPerson) {
                            System.out.println("You cannot set yourself to dead.");
                        } else if (personToSetDead.isDead()) {
                            System.out.println("Person is already dead.");
                        } else {
                            personToSetDead.setDead(reader, cemeteries);
                            System.out.println("Successfully set " + personToSetDead.getFullName() + " to dead.");
                        }
                    }
                }

                // Set person to alive
                else if (command.matches("(?i)^set alive.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to edit people.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person personToSetAlive = selectPersonFromCommand(reader, command, 2, selectedPeople, true);

                    if (personToSetAlive != null) {
                        if (personToSetAlive.isDead()) {
                            personToSetAlive.setAlive();
                            System.out.println("Successfully set " + personToSetAlive.getFullName() + " to alive.");
                        } else {
                            System.out.println("Person is not dead.");
                        }
                    }
                }

                // Set admin or user
                else if (command.matches("(?i)^set (admin|user).*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to edit people.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person personToSet = selectPersonFromCommand(reader, command, 2, selectedPeople, true);

                    if (personToSet != null) {
                        if (personToSet.isDead()) {
                            System.out.println("You cannot set a dead person to admin or user.");
                            continue;
                        }

                        if (command.matches("(?i)^set admin.*$")) {
                            personToSet.setAdmin(true);
                            System.out.println("Successfully set " + personToSet.getFullName() + " to admin.");
                        } else if (command.matches("(?i)^set user.*$")) {
                            personToSet.setAdmin(false);
                            System.out.println("Successfully set " + personToSet.getFullName() + " to user.");
                        }
                    }
                }

                // Search Person
                else if (command.matches("(?i)^search person.*$")) {
                    if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Map<String, String> args = ConsoleReader.parseArguments(command);

                    selectedPeople = searchPeopleByCommand(command, command.contains("include alive"));

                    if (args.containsKey("sort_by")) {
                        selectedPeople.sort(Person.getComparator(args.get("sort_by")));
                    }

                    System.out.println("Found " + selectedPeople.size() + " people. Use view <number> to view details.");

                    System.out.println(Person.toRowHeader());
                    for (int i = 0; i < selectedPeople.size(); i++) {
                        System.out.println(selectedPeople.get(i).toRowString(i + 1));
                    }
                    viewMode = ViewMode.PERSONS;
                }

                // Search Relatives
                else if (command.matches("(?i)^search relatives.*$")) {
                    Map<String, String> argsMap = ConsoleReader.parseArguments(command);

                    int generationInterval = 2;
                    if (!argsMap.containsKey("interval")) {
                        System.out.println("Using default generation interval of " + generationInterval + ".");
                    } else {
                        try {
                            generationInterval = Integer.parseInt(argsMap.get("interval"));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid generation interval. Please enter a number.");
                            continue;
                        }
                    }

                    Person personToSearch;

                    if (!argsMap.isEmpty()) {
                        if (!selectedPerson.isAdmin()) {
                            System.out.println("You do not have permission to search other people's relatives.");
                            personToSearch = selectedPerson;
                        } else {
                            personToSearch = selectPersonFromCommand(reader, command, 2, selectedPeople, true);

                            if (personToSearch == null) {
                                System.out.println("Person not found.");
                                continue;
                            }
                        }
                    } else {
                        personToSearch = selectedPerson;
                    }

                    List<PersonRelationship> result = searchRelativesRecursive(generationInterval, personToSearch);

                    if (result.isEmpty()) {
                        System.out.println("No relatives found.");
                    } else {
                        System.out.println("Found " + result.size() + " relatives of " + personToSearch.getFullName() + ". Use view <number> to view details.");
                        System.out.printf("    %-12s %-12s  %s\n", "Name", "Surname", "Relationship");
                        selectedPeople = new ArrayList<>(result.size());
                        for (int i = 0; i < result.size(); i++) {
                            PersonRelationship personRelationship = result.get(i);
                            selectedPeople.add(personRelationship.person);
                            System.out.printf("%-3d %-12s %-12s %s\n", i + 1, personRelationship.person.getName(), personRelationship.person.getSurname(), personRelationship.relationship);
                        }
                    }
                    viewMode = ViewMode.PERSONS;
                }

                // Visit Person
                else if (command.matches("(?i)^visit person.*$")) {
                    if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person foundPerson = selectPersonFromCommand(reader, command, 2, selectedPeople, false);

                    if (foundPerson != null && foundPerson.isDead()) {
                        if (foundPerson.getCemetery() == null) {
                            System.out.println("Can not find the cemetery of " + foundPerson.getName() + " " + foundPerson.getSurname() + ".");
                            continue;
                        }
                        foundPerson.getCemetery().addVisitor(foundPerson, selectedPerson, new Date());
                        System.out.println("Successfully visited " + foundPerson.getName() + " " + foundPerson.getSurname() + " in " + foundPerson.getCemetery().getName() + " by " + selectedPerson.getName() + " " + selectedPerson.getSurname() + ".");
                    } else {
                        if (foundPerson != null && !foundPerson.isDead())
                            System.out.println("Person is not dead.");
                        else
                            System.out.println("Can not find person to visit.");
                    }
                }

                // Get Visitor List
                else if (command.matches("(?i)^get visitor list.*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to get visitor list.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    Person foundPerson = selectPersonFromCommand(reader, command, 3, selectedPeople, false);

                    if (foundPerson != null) {
                        if (foundPerson.getCemetery() == null) {
                            System.out.println("Can not find the cemetery of " + foundPerson.getFullName() + ".");
                            continue;
                        }
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
                }

                // Add Cemetery
                else if (command.equalsIgnoreCase("add cemetery")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to add cemeteries.");
                        continue;
                    }

                    Cemetery newCemetery = new Cemetery(reader, cemeteries);
                    cemeteries.put(newCemetery.getId(), newCemetery);
                    newCemetery.connect(people);
                    System.out.println("Successfully added cemetery \"" + newCemetery.getName() + "\".");
                }

                // Remove Cemetery
                else if (command.matches("(?i)^remove cemetery .*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to remove cemeteries.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter cemetery ID.");
                        continue;
                    }

                    Cemetery cemeteryToRemove = selectCemeteryFromCommand(reader, command, 2, selectedCemeteries);

                    if (cemeteryToRemove != null) {
                        String answer = reader.getAnswer(ConsoleReader.yesNo(
                                "There are " + cemeteryToRemove.getCount() + " people in this cemetery. Confirm removal?"));

                        if (answer.matches(ConsoleReader.YES_REGEX)) {
                            cemeteries.remove(cemeteryToRemove.getId());
                            System.out.println("Successfully removed cemetery with ID " + cemeteryToRemove.getId() + ".");
                        } else {
                            throw new CancellationException();
                        }
                    }
                }

                // Edit Cemetery
                else if (command.matches("(?i)^edit cemetery .*$")) {
                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to edit cemeteries.");
                        continue;
                    } else if (command.split(" ").length < 3) {
                        System.out.println("Please enter cemetery ID.");
                        continue;
                    }

                    Cemetery editedCemetery = selectCemeteryFromCommand(reader, command, 2, selectedCemeteries);

                    if (editedCemetery != null) {
                        editedCemetery.edit(reader);
                        System.out.println("Successfully updated cemetery.");
                    } else {
                        System.out.println("Cemetery not found.");
                    }
                }

                // Search Cemetery
                else if (command.matches("(?i)^search cemetery.*$")) {
                    if (command.split(" ").length < 3) {
                        System.out.println("Please enter at least one search criteria.");
                        continue;
                    }

                    selectedCemeteries = searchCemeteriesByCommand(command);

                    if (selectedCemeteries.isEmpty()) {
                        System.out.println("No cemeteries found.");
                    } else {
                        System.out.println("Found " + selectedCemeteries.size() + " cemeteries:");
                        System.out.println(Cemetery.toRowHeader());
                        for (int i = 0; i < selectedCemeteries.size(); i++) {
                            System.out.println(selectedCemeteries.get(i).toRowString(i + 1));
                        }
                    }

                    viewMode = ViewMode.CEMETERIES;
                }

                // View Person or Cemetery
                else if (command.matches("(?i)^view.*$")) {
                    if (command.split(" ").length < 2) {
                        System.out.println("Please enter a number.");
                        continue;
                    }

                    if (command.contains("person")) {
                        viewMode = ViewMode.PERSONS;
                    } else if (command.contains("cemetery")) {
                        viewMode = ViewMode.CEMETERIES;
                    }

                    if (viewMode == ViewMode.PERSONS) {
                        Person personToView = selectPersonFromCommand(reader, command, 1, selectedPeople, true);

                        if (personToView != null) {
                            System.out.println(personToView.toDetailString(selectedPerson.isAdmin()));
                        }
                    } else {
                        Cemetery cemeteryToView = selectCemeteryFromCommand(reader, command, 1, selectedCemeteries);

                        if (cemeteryToView != null) {
                            System.out.println(cemeteries.get(cemeteryToView.getId()).toDetailString(selectedPerson.isAdmin()));
                        }
                    }
                }

                // Show statistics
                else if (command.matches("(?i)^show statistics.*$")) {
                    Map<String, String> argsMap = ConsoleReader.parseArguments(command);

                    if (!selectedPerson.isAdmin()) {
                        System.out.println("You do not have permission to view statistics.");
                    } else if (command.equalsIgnoreCase("show statistics")) {
                        System.out.println(showStatistics(null));
                    } else if (command.matches("(?i)^.*group.*$")) {
                        System.out.println(showStatistics(new Cemetery(argsMap.get("id"), argsMap.get("name"), new Address(argsMap.get("country"), argsMap.get("city"), argsMap.get("district"), argsMap.get("neighbourhood"), argsMap.get("street")))));
                    } else {
                        Cemetery cemeteryToView = selectCemeteryFromCommand(reader, command, 2, selectedCemeteries);

                        if (cemeteryToView != null) {
                            System.out.println(cemeteryToView.getStatistics(people.values().iterator()));
                        }
                    }
                }

                // Incorrect Command
                else if (!command.isBlank() && !command.matches("(?i)^quit|exit$")) {
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
