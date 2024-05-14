import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Cemetree c = new Cemetree();
        c.loadFromFile("people_50k.csv", "cemeteries.csv");

        c.consoleMode();

//        c.searchPeopleByDate(new Date(01, 01, 1970), new Date(01, 01, 2000));
//        c.saveToFile("people_50k_saved.csv", "cemeteries_saved.csv");
    }
}
