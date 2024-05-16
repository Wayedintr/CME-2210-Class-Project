import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Date extends GregorianCalendar {

    final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    final SimpleDateFormat FORMAT_CLK = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    final static String REGEX = "^(((0[1-9]|[12]\\d|3[01])[-\\/](0[13578]|1[02])[-\\/]((1[5-9]\\d{2})|(2\\d{3})))|((0[1-9]|[12]\\d|30)[-\\/](0[13456789]|1[012])[-\\/]((1[5-9]\\d{2})|(2\\d{3})))|((0[1-9]|1\\d|2[0-8])[-\\/]02[-\\/]((1[5-9]\\d{2})|(2\\d{3})))|(29[-\\/]02[-\\/]((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))))$";

    Date(int day, int month, int year) {
        super(year, month, day);
    }

    Date(String date) {
        String[] data = date.split("[-/]");
        set(Calendar.YEAR, Integer.parseInt(data[2]));
        set(Calendar.MONTH, Integer.parseInt(data[1]) - 1);
        set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[0]));
    }

    Date(String year, String month, String day) {
        super(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
    }

    Date() {
        super();
    }

    public String toString() {
        return FORMAT.format(this.getTime());
    }

    public String toStringCLK() {
        return FORMAT_CLK.format(this.getTime());
    }
}
