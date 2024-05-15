import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CancellationException;

public class ConsoleReader {

    private final Scanner scanner;

    ConsoleReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getAnswer(Question question) throws CancellationException {
        String answer;
        do {
            System.out.print(question.label + ": ");
            answer = scanner.nextLine();

            if (answer.isBlank()) {
                if (question.isRequired)
                    System.out.print("This field is required.\n");
                else
                    break;
            } else if (answer.matches("cancel|quit|exit")) {
                throw new CancellationException();
            } else if (!answer.matches(question.regex)) {
                System.out.print(question.errorMessage + "\n");
            }
        } while (!answer.matches(question.regex) || (question.isRequired && answer.isBlank()));

        return answer;
    }

    public static Map<String, String> parseArguments(String command) {
        Map<String, String> argsMap = new HashMap<>();
        String[] args = command.split(" ");

        for (String arg : args) {
            String[] pair = arg.split("=");
            if (pair.length == 2) {
                argsMap.put(pair[0], pair[1]);
            }
        }

        return argsMap;
    }

    public record Question(String label, String regex, String errorMessage, boolean isRequired) {

    }

    public static final Question YES_NO = new Question("Yes or No", "(?i)^(yes|no|y|n)$", "Please enter 'yes' or 'no'.", true);

    public static final String YES_REGEX = "(?i)^(yes|y)$";

    public static Question yesNo(String label) {
        return new Question(label, YES_NO.regex, YES_NO.errorMessage, YES_NO.isRequired);
    }
}
