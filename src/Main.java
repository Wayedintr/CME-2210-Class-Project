import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Cemetree c = new Cemetree();

        c.loadFromFile("saved");
        c.consoleMode();
        c.saveToFile("saved");
    }
}
