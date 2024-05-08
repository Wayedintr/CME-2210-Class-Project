import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Person {
    private String name, surname, id, sex;
    private GregorianCalendar birthDate, deathDate;

    boolean dead, admin;

    private Cemetery cemetery;

    private String deathCause;

    private String motherId, fatherId, spouseId;

    private Person mother, father, spouse;

    private final List<Person> children;

    Person(String id, String name, String surname, String sex, boolean admin, boolean dead, String deathCause, Cemetery cemetery, GregorianCalendar birthDate, GregorianCalendar deathDate, String motherId, String fatherId, String spouseId) {
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

        this.children = new ArrayList<>();
    }

    Person(String id, String name, String surname, String sex, boolean admin, boolean dead, String deathCause, Cemetery cemetery, GregorianCalendar birthDate, GregorianCalendar deathDate) {
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

        this.children = new ArrayList<>();
    }

    public String toString() {
        return name + " " + surname + " " + id;
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

    public GregorianCalendar getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(GregorianCalendar birthDate) {
        this.birthDate = birthDate;
    }

    public GregorianCalendar getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(GregorianCalendar deathDate) {
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
