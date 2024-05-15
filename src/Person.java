import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
            new ConsoleReader.Question("Death Date", Date.REGEX, "Invalid death date. Must be in the format DD/MM/YYYY or DD-MM-YYYY", false),
            new ConsoleReader.Question("Death Cause", "^[\\p{L}\\p{M}'-]{2,64}$", "Invalid death cause. Must contain only letters, 2-64 characters.", false),
            new ConsoleReader.Question("Cemetery ID", "[0-9]{2}-[0-9]{3}", "Invalid cemetery ID. Must be in the format 'XX-XXX", true)
    );

    Person(final Scanner scanner, final Map<String, Person> people, final Map<String, Cemetery> cemeteries) throws CancellationException {
        ConsoleReader reader = new ConsoleReader(scanner);

        String id;
        for (id = reader.getAnswer(QUESTIONS.getFirst()); people.containsKey(id); id = reader.getAnswer(QUESTIONS.getFirst())) {
            System.out.println("ID already exists. Please enter a different ID.");
        }
        this.id = id;

        this.name = reader.getAnswer(QUESTIONS.get(1)).toUpperCase();
        this.surname = reader.getAnswer(QUESTIONS.get(2)).toUpperCase();
        this.sex = reader.getAnswer(QUESTIONS.get(3)).matches("(?i)^(male|m)$") ? "Male" : "Female";
        this.birthDate = new Date(reader.getAnswer(QUESTIONS.get(4)));

        this.motherId = reader.getAnswer(QUESTIONS.get(5));
        this.fatherId = reader.getAnswer(QUESTIONS.get(6));
        this.spouseId = reader.getAnswer(QUESTIONS.get(7));

        this.dead = reader.getAnswer(QUESTIONS.get(8)).matches("(?i)^(true|t)$");
        if (this.dead) {
            Date deathDate = new Date(reader.getAnswer(QUESTIONS.get(9)));
            while (deathDate.before(this.birthDate)) {
                System.out.println("Death date cannot be before birth date. Please enter a valid death date.");
                deathDate = new Date(reader.getAnswer(QUESTIONS.get(9)));
            }
            this.deathDate = deathDate;

            this.deathCause = reader.getAnswer(QUESTIONS.get(10));

            String cemeteryId;
            for (cemeteryId = reader.getAnswer(QUESTIONS.get(11)); !cemeteries.containsKey(cemeteryId); cemeteryId = reader.getAnswer(QUESTIONS.get(11))) {
                System.out.println("Cemetery ID does not exist. Please enter a different ID.");
            }
            this.cemetery = cemeteries.get(cemeteryId);
        }

    }

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

    Person(String id, String name, String surname, String sex, boolean admin, boolean dead, String deathCause, Cemetery cemetery, Date birthDate, Date deathDate) {
        this.name = name;
        this.surname = surname;
        this.id = id;
        this.admin = admin;
        this.sex = sex;
        this.birthDate = birthDate;
        this.dead = dead;
        this.deathDate = deathDate;
        this.cemetery = cemetery;
        this.deathCause = deathCause;
    }

    public void remove() {
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
        return "ID: " + id + "\n" +
                "Name: " + name + "\n" +
                "Surname: " + surname + "\n" +
                "Sex: " + sex + "\n" +
                "Admin: " + admin + "\n" +
                "Dead: " + dead + "\n" +
                "Death Cause: " + deathCause + "\n" +
                "Cemetery ID: " + (cemetery == null ? "" : cemetery.getId()) + "\n" +
                "Birth Date: " + birthDate + "\n" +
                "Death Date: " + deathDate + "\n" +
                "Mother ID: " + motherId + "\n" +
                "Father ID: " + fatherId + "\n" +
                "Spouse ID: " + spouseId + "\n";
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
        if (cemetery != null && cemetery.getId().equals(id)) {
            this.cemetery.decrementCount();
            this.cemetery = null;
        }
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
