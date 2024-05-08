import graph.DirectedGraph;
import graph.GraphInterface;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Cemetree c = new Cemetree();

        c.loadFromFile("people_50k.csv");
    }
}
