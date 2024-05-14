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
            System.out.print(question.label() + ": ");
            answer = scanner.nextLine();

            if (answer.isBlank()) {
                if (question.isRequired())
                    System.out.print("This field is required.\n");
                else
                    break;
            } else if (answer.equals("cancel")) {
                throw new CancellationException();
            } else if (!answer.matches(question.regex())) {
                System.out.print(question.errorMessage() + "\n");
            }
        } while (!answer.matches(question.regex()) || (question.isRequired() && answer.isBlank()));

        return answer;
    }

    public record Question(String label, String regex, String errorMessage, boolean isRequired) {

    }
}
