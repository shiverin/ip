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
        System.out.println(DIVIDER);
        System.out.println("Hello! I'm Nimbus.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                break;
            }
            System.out.println(command);
            System.out.println(DIVIDER);
        }
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
        scanner.close();
    }
}
