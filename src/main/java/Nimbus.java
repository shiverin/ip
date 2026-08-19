import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs Nimbus, a personal task assistant.
 */
public class Nimbus {
    private static final String DIVIDER = "____________________________________________________________";

    /**
     * Greets the user, echoes commands, and exits when the user enters {@code bye}.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> tasks = new ArrayList<>();
        System.out.println(DIVIDER);
        System.out.println("Hello! I'm Nimbus.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                break;
            }
            if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ". " + tasks.get(i));
                }
            } else {
                tasks.add(command);
                System.out.println("Added: " + command);
            }
            System.out.println(DIVIDER);
        }
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
        scanner.close();
    }
}
