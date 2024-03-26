import java.util.Date;

public class Person {
    private String name, surname, id, sex;
    private Date birthDate, deathDate;

    boolean dead;

    private Cemetery cemetery;

    private String deathCause;

    private String motherId, fatherId;

    Person(String name, String surname, String id, String sex, Date birthDate, String motherId, String fatherId, boolean dead, Date deathDate, Cemetery cemetery, String deathCause) {
        this.name = name;
        this.surname = surname;
        this.id = id;
        this.sex = sex;
        this.birthDate = birthDate;
        this.motherId = motherId;
        this.fatherId = fatherId;
        this.dead = dead;
        this.deathDate = deathDate;
        this.cemetery = cemetery;
        this.deathCause = deathCause;
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
}
