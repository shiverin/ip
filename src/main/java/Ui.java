import java.util.List;
import java.util.Scanner;

/** Handles all console interaction with the user. */
public class Ui implements AutoCloseable {
    private static final String DIVIDER = "____________________________________________________________";
    private final Scanner scanner = new Scanner(System.in);

    /** Displays the welcome message. */
    public void showWelcome() {
        showLine();
        show("Hello! I'm Nimbus.");
        show("What can I do for you?");
        showLine();
    }

    /** Returns whether another line of input is available. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads the next user command. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays a message. */
    public void show(String message) {
        System.out.println(message);
    }

    /** Displays the task list with one-based numbering. */
    public void showTasks(String heading, List<Task> tasks) {
        show(heading);
        for (int i = 0; i < tasks.size(); i++) {
            show((i + 1) + ". " + tasks.get(i));
        }
    }

    /** Displays an input error. */
    public void showError(String message) {
        show("I couldn't do that: " + message);
    }

    /** Displays the closing message. */
    public void showGoodbye() {
        show("Bye. Hope to see you again soon!");
        showLine();
    }

    /** Displays a horizontal divider. */
    public void showLine() {
        show(DIVIDER);
    }

    @Override
    public void close() {
        scanner.close();
    }
}
