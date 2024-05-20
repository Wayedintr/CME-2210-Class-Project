import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

public class Person {
    private String name, surname, id, sex;
    private Date birthDate, deathDate;
    boolean dead, admin;

    private Cemetery cemetery;

    private String deathCause;

    private String motherId, fatherId, spouseId;

    private Person mother, father, spouse;

    private final List<Person> children = new ArrayList<>();

    public static final List<ConsoleReader.Question> QUESTIONS = List.of(
            new ConsoleReader.Question("ID", "[0-9]{11}", "Invalid ID.", true),
            new ConsoleReader.Question("Name", "^[\\p{L}\\p{M}'-]{2,64}$", "Invalid name. Must contain only letters, 2-64 characters.", true),
            new ConsoleReader.Question("Surname", "^[\\p{L}\\p{M}'-]{2,64}$", "Invalid surname. Must contain only letters, 2-64 characters.", true),
            new ConsoleReader.Question("Sex", "(?i)^(male|female|m|f)$", "Invalid sex. Must be 'male' or 'female'.", false),
            new ConsoleReader.Question("Birthdate", Date.REGEX, "Invalid birthdate. Must be in the format DD/MM/YYYY or DD-MM-YYYY", true),
            new ConsoleReader.Question("Mother ID", "[0-9]{11}", "Invalid mother ID.", false),
            new ConsoleReader.Question("Father ID", "[0-9]{11}", "Invalid father ID.", false),
            new ConsoleReader.Question("Spouse ID", "[0-9]{11}", "Invalid spouse ID.", false),
            new ConsoleReader.Question("Dead", "(?i)^(true|false|t|f)$", "Invalid dead. Must be 'true' or 'false'.", true),
            new ConsoleReader.Question("Death Date", Date.REGEX, "Invalid death date. Must be in the format DD/MM/YYYY or DD-MM-YYYY", true),
            new ConsoleReader.Question("Death Cause", "^[\\p{L}\\p{M}'\\s-]{2,64}$", "Invalid death cause. Must contain only letters, 2-64 characters.", false),
            new ConsoleReader.Question("Cemetery ID", "[0-9]{2}-[0-9]{3}", "Invalid cemetery ID. Must be in the format 'XX-XXX", true)
    );

    Person(String id, String name, String surname, String sex, boolean admin, boolean dead, String deathCause, Cemetery cemetery, Date birthDate, Date deathDate, String motherId, String fatherId, String spouseId) {
        this.name = name;
        this.surname = surname;
        this.id = id;
        this.admin = admin;
        this.sex = sex;
        this.birthDate = birthDate;
        this.motherId = motherId;
        this.fatherId = fatherId;
        this.spouseId = spouseId;
        this.dead = dead;
        this.deathDate = deathDate;
        this.cemetery = cemetery;
        this.deathCause = deathCause;
    }

    Person(String id, String name, String surname, String sex, String deathCause, Cemetery cemetery, Date birthDate, Date deathDate) {
        this.name = name;
        this.surname = surname;
        this.id = id;
        this.sex = sex;
        this.cemetery = cemetery;
        this.deathCause = deathCause;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
    }

    Person(ConsoleReader reader, Map<String, Person> people, Map<String, Cemetery> cemeteries) throws CancellationException {
        String id;
        for (id = reader.getAnswer(QUESTIONS.get(0), 12); people.containsKey(id); id = reader.getAnswer(QUESTIONS.get(0), 12)) {
            System.out.println("ID already exists. Please enter a different ID.");
        }
        this.id = id;

        this.name = reader.getAnswer(QUESTIONS.get(1), 12).toUpperCase();
        this.surname = reader.getAnswer(QUESTIONS.get(2), 12).toUpperCase();
        this.sex = reader.getAnswer(QUESTIONS.get(3), 12).matches("(?i)^(male|m)$") ? "Male" : "Female";
        this.birthDate = new Date(reader.getAnswer(QUESTIONS.get(4), 12));

        this.motherId = reader.getAnswer(QUESTIONS.get(5), 12);
        this.fatherId = reader.getAnswer(QUESTIONS.get(6), 12);
        this.spouseId = reader.getAnswer(QUESTIONS.get(7), 12);

        this.dead = reader.getAnswer(QUESTIONS.get(8), 12).matches("(?i)^(true|t)$");
        if (this.dead) {
            getDeathQuestions(reader, cemeteries, 12);
        }
    }

    public void getDeathQuestions(ConsoleReader reader, Map<String, Cemetery> cemeteries, int labelSize) throws CancellationException {
        String deathDateStr = reader.getAnswer(QUESTIONS.get(9), labelSize);
        Date deathDate = deathDateStr.isBlank() ? null : new Date(deathDateStr);
        while (deathDate != null && (deathDate.before(this.birthDate) || deathDate.after(new Date()))) {
            if (deathDate.before(this.birthDate))
                System.out.println("Death date cannot be before birth date. Please enter a valid death date.");
            else if (deathDate.after(new Date()))
                System.out.println("Death date cannot be in the future. Please enter a valid death date.");

            deathDate = new Date(reader.getAnswer(QUESTIONS.get(9), labelSize));
        }
        this.deathDate = deathDate;

        this.deathCause = reader.getAnswer(QUESTIONS.get(10), labelSize);

        String cemeteryId;
        for (cemeteryId = reader.getAnswer(QUESTIONS.get(11), labelSize); !cemeteries.containsKey(cemeteryId); cemeteryId = reader.getAnswer(QUESTIONS.get(11), labelSize)) {
            System.out.println("Cemetery ID does not exist. Please enter a different ID.");
        }
        this.cemetery = cemeteries.get(cemeteryId);
        this.cemetery.incrementCount();

        this.dead = true;
    }

    public void setDead(ConsoleReader reader, Map<String, Cemetery> cemeteries) throws CancellationException {
        setDead(reader, cemeteries, 0);
    }

    private void setDead(ConsoleReader reader, Map<String, Cemetery> cemeteries, int labelSize) throws CancellationException {
        getDeathQuestions(reader, cemeteries, labelSize);
    }

    public void setAlive() {
        this.dead = false;
        this.deathDate = null;
        this.deathCause = null;
        if (this.cemetery != null)
            this.cemetery.decrementCount();
        this.cemetery = null;
    }

    public void edit(ConsoleReader reader, Map<String, Person> people, Map<String, Cemetery> cemeteries) throws CancellationException {
        boolean editDeath = false;
        boolean removeDeathInfo = false;

        String name, surname, sex, birthDateStr, motherId, fatherId, spouseId, dead, deathDateStr, deathCause, cemeteryId;
        Date birthDate, deathDate;

        name = reader.getAnswer(QUESTIONS.get(1).withLabel("Name (" + this.name + ")").withRequired(false), 24);
        surname = reader.getAnswer(QUESTIONS.get(2).withLabel("Surname (" + this.surname + ")").withRequired(false), 24);
        sex = reader.getAnswer(QUESTIONS.get(3).withLabel("Sex (" + this.sex + ")").withRequired(false), 24);
        birthDateStr = reader.getAnswer(QUESTIONS.get(4).withLabel("Birth Date (" + this.birthDate + ")").withRequired(false), 24);
        birthDate = birthDateStr.isBlank() ? this.birthDate : new Date(birthDateStr);
        while (birthDate != null && !birthDate.equals(this.birthDate) && birthDate.after(new Date())) {
            System.out.println("Birth date cannot be in the future. Please enter a valid birth date.");
            birthDateStr = reader.getAnswer(QUESTIONS.get(4).withLabel("Birth Date (" + this.birthDate + ")").withRequired(false), 24);
            birthDate = birthDateStr.isBlank() ? this.birthDate : new Date(birthDateStr);
        }

        motherId = reader.getAnswer(QUESTIONS.get(5).withLabel("Mother ID (" + this.motherId + ")").withRequired(false), 24);
        fatherId = reader.getAnswer(QUESTIONS.get(6).withLabel("Father ID (" + this.fatherId + ")").withRequired(false), 24);
        spouseId = reader.getAnswer(QUESTIONS.get(7).withLabel("Spouse ID (" + this.spouseId + ")").withRequired(false), 24);
        dead = reader.getAnswer(QUESTIONS.get(8).withLabel("Dead (" + this.dead + ")").withRequired(false), 24);

        if (dead.matches("(?i)^(true|t)$")) {
            if (this.dead)
                editDeath = true;
            else
                this.setDead(reader, cemeteries, 24);
        } else if (dead.matches("(?i)^(false|f)$")) {
            removeDeathInfo = true;
        } else if (dead.isBlank()) {
            if (this.dead)
                editDeath = true;
            else
                removeDeathInfo = true;
        }

        this.name = name.isBlank() ? this.name : name;
        this.surname = surname.isBlank() ? this.surname : surname;
        this.sex = sex.isBlank() ? this.sex : sex;
        this.birthDate = birthDate;
        this.motherId = motherId.isBlank() ? this.motherId : motherId;
        this.fatherId = fatherId.isBlank() ? this.fatherId : fatherId;
        this.spouseId = spouseId.isBlank() ? this.spouseId : spouseId;

        removeConnections();
        connect(people, cemeteries);

        if (editDeath) {
            deathDateStr = reader.getAnswer(QUESTIONS.get(9).withLabel("Death Date (" + this.deathDate + ")").withRequired(false), 24);
            deathDate = deathDateStr.isBlank() ? this.deathDate : new Date(deathDateStr);
            while (deathDate != null && (deathDate.before(this.birthDate) || deathDate.after(new Date()))) {
                if (deathDate.before(this.birthDate))
                    System.out.println("Death date cannot be before birth date. Please enter a valid death date.");
                else if (deathDate.after(new Date()))
                    System.out.println("Death date cannot be in the future. Please enter a valid death date.");

                deathDate = new Date(reader.getAnswer(QUESTIONS.get(9).withLabel("Death Date (" + this.deathDate + ")").withRequired(false), 24));
            }

            deathCause = reader.getAnswer(QUESTIONS.get(10).withLabel("Death Cause (" + this.deathCause + ")").withRequired(false), 24);
            cemeteryId = reader.getAnswer(QUESTIONS.get(11).withLabel("Cemetery ID (" + this.cemetery.getId() + ")").withRequired(false), 24);

            this.deathDate = deathDate;
            this.deathCause = deathCause.isBlank() ? this.deathCause : deathCause;
            this.cemetery = cemeteryId.isBlank() ? this.cemetery : cemeteries.get(cemeteryId);
        } else if (removeDeathInfo) {
            setAlive();
        }
    }

    public void connect(Map<String, Person> people, Map<String, Cemetery> cemeteries) {
        if (this.hasMotherId() && people.get(this.getMotherId()) != null) {
            Person mother = people.get(this.getMotherId());
            this.setMother(mother);
            mother.addChild(this);
        }
        if (this.hasFatherId() && people.get(this.getFatherId()) != null) {
            Person father = people.get(this.getFatherId());
            this.setFather(father);
            father.addChild(this);
        }
        if (this.hasSpouseId() && people.get(this.getSpouseId()) != null) {
            Person spouse = people.get(this.getSpouseId());
            if (spouse != null) {
                this.setSpouse(spouse);
                spouse.setSpouse(this);
            }
        }
        if (this.getCemetery() != null && this.isDead()) {
            if (cemetery.count >= cemetery.CAPACITY) {
                System.out.println("Cemetery " + cemetery.getId() + " is full. Person " + this.getName() + " " + this.getSurname() + " cannot be added.");
            } else {
                people.put(this.getId(), this);
            }
            cemetery.incrementCount();
        } else {
            people.put(this.getId(), this);
        }
    }

    public void removeConnections() {
        if (this.cemetery != null) this.cemetery.decrementCount();
        if (this.father != null) this.father.getChildren().remove(this);
        if (this.mother != null) this.mother.getChildren().remove(this);
        this.spouse = null;
        for (Person child : this.getChildren()) {
            if (this.sex.equals("Male")) {
                child.setFather(null);
            } else {
                child.setMother(null);
            }
        }
    }

    public String toString() {
        return String.format("Person(%s,%s %s)", id, name, surname);
    }

    public static String toCsvHeader() {
        return "id,name,surname,sex,admin,dead,deathCause,cemeteryId,birthDate,deathDate,motherId,fatherId,spouseId";
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                id == null ? "" : id,
                name == null ? "" : name,
                surname == null ? "" : surname,
                sex == null ? "" : sex,
                admin ? "1" : "0",
                dead ? "1" : "0",
                deathCause == null ? "" : deathCause,
                cemetery == null ? "" : cemetery.getId(),
                birthDate == null ? "" : birthDate,
                deathDate == null ? "" : deathDate,
                motherId == null ? "" : motherId,
                fatherId == null ? "" : fatherId,
                spouseId == null ? "" : spouseId
        );
    }

    public static String toRowHeader() {
        return String.format("    %-12s %-12s %s", "Name", "Surname", "Birth date - Death date");
    }

    public String toRowString(int index) {
        return String.format("%-3d %-12s %-12s %s", (index), name, surname, getDateString());
    }

    public String toDetailString(boolean admin) {
        return (admin ?
                "ID          : " + id : "") +
                "\nName        : " + name + " " + surname +
                "\nSex         : " + sex +
                "\nBorn        : " + birthDate +
                (deathDate != null ? "\nDied        : " + deathDate : "") +
                (deathCause != null && !deathCause.isBlank() ? "\nDeath Cause : " + deathCause : "") +
                (cemetery != null ? "\nCemetery    : " + cemetery.getName() : "") +
                (mother != null ? "\nMother      : " + mother.getFullName() : "") +
                (father != null ? "\nFather      : " + father.getFullName() : "") +
                (spouse != null ? "\nSpouse      : " + spouse.getFullName() : "");
    }

    public boolean matches(Person filter) {
        if (filter == null) return true;
        if (filter.name != null && !filter.name.equalsIgnoreCase(this.name)) return false;
        if (filter.surname != null && !filter.surname.equalsIgnoreCase(this.surname)) return false;
        if (filter.id != null && !filter.id.equals(this.id)) return false;
        if (filter.sex != null && !filter.sex.equalsIgnoreCase(this.sex)) return false;
        if (filter.birthDate != null && !filter.birthDate.equals(this.birthDate)) return false;
        if (filter.deathDate != null && !filter.deathDate.equals(this.deathDate)) return false;
        if (filter.cemetery != null && !filter.cemetery.equals(this.cemetery)) return false;
        if (filter.deathCause != null && !filter.deathCause.equalsIgnoreCase(this.deathCause)) return false;
        return true;
    }


    public int compareTo(Person person) {
        return birthDate.compareTo(person.getBirthDate());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public double getAge() {
        if (deathDate == null) {
            return (new Date().getTimeInMillis() - birthDate.getTimeInMillis()) / 1000.0 / 60.0 / 60.0 / 24.0 / 365.25;
        } else {
            return (deathDate.getTimeInMillis() - birthDate.getTimeInMillis()) / 1000.0 / 60.0 / 60.0 / 24.0 / 365.25;
        }
    }

    public String getDateString() {
        return (birthDate == null ? "unknown" : birthDate.toString()) + " - " + (deathDate == null ? "present" : deathDate.toString());
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public Cemetery getGraveyard() {
        return cemetery;
    }

    public void setGraveyard(Cemetery cemetery) {
        this.cemetery = cemetery;
    }

    public String getDeathCause() {
        return deathCause;
    }

    public void setDeathCause(String deathCause) {
        this.deathCause = deathCause;
    }

    public String getMotherId() {
        return motherId;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getFatherId() {
        return fatherId;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public String getMotherId(Person person) {
        return person.motherId;
    }

    public String getFatherId(Person person) {
        return person.fatherId;
    }

    public String getSpouseId() {
        return spouseId;
    }

    public void setSpouseId(String spouseId) {
        this.spouseId = spouseId;
    }

    public Cemetery getCemetery() {
        return cemetery;
    }

    public void setCemetery(Cemetery cemetery) {
        this.cemetery = cemetery;
    }

    public void removeCemeteryIfId(String id) {
//        if (cemetery != null && cemetery.getId().equals(id)) {
//            this.cemetery.decrementCount();
//            this.cemetery = null;
//        }
    }

    public Person getMother() {
        return mother;
    }

    public void setMother(Person mother) {
        this.mother = mother;
    }

    public Person getFather() {
        return father;
    }

    public void setFather(Person father) {
        this.father = father;
    }

    public Person getSpouse() {
        return spouse;
    }

    public void setSpouse(Person spouse) {
        this.spouse = spouse;
    }

    public boolean hasMotherId() {
        return motherId != null;
    }

    public boolean hasFatherId() {
        return fatherId != null;
    }

    public boolean hasSpouseId() {
        return spouseId != null;
    }

    public void addChild(Person child) {
        children.add(child);
    }

    public List<Person> getChildren() {
        return children;
    }
}
