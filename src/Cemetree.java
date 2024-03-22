import graph.DirectedGraph;

import java.util.HashMap;

public class Cemetree {

    private enum Relationship {
        MOTHER,
        FATHER
    }

    HashMap<String, Person> people;
    DirectedGraph<Person, Relationship> graph;

    public Cemetree() {
        people = new HashMap<>();
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
}
