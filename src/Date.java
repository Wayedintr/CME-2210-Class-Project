import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Date extends GregorianCalendar {

    final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    Date(int day, int month, int year) {
        super(year, month, day);
    }

    Date(String date) {
        String[] data = date.split("/");
        set(Calendar.YEAR, Integer.parseInt(data[2]));
        set(Calendar.MONTH, Integer.parseInt(data[1]) - 1);
        set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[0]));
    }

    Date(String year, String month, String day) {
        super(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
    }

    public String toString() {
        return FORMAT.format(this.getTime());
    }
}
