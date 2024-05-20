import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Cemetree c = new Cemetree();
        final String FILENAME = "saved";

        try {
            c.loadFromFile(FILENAME);
        } catch (IOException ignored) {
            return;
        }

        c.consoleMode();

        try {
            c.saveToFile(FILENAME);
        } catch (IOException e) {
            System.out.println("Failed to save");
            throw e;
        }
    }
}
