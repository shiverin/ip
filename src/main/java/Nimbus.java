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
        ArrayList<Task> tasks = new ArrayList<>();
        System.out.println(DIVIDER);
        System.out.println("Hello! I'm Nimbus.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.equals("bye")) {
                break;
            }
            try {
                processCommand(command, tasks);
            } catch (NimbusException e) {
                System.out.println("I couldn't do that: " + e.getMessage());
            }
            System.out.println(DIVIDER);
        }
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
        scanner.close();
    }

    private static void processCommand(String command, ArrayList<Task> tasks) throws NimbusException {
        switch (CommandType.from(command)) {
            case LIST -> showTasks(tasks);
            case MARK -> updateTaskStatus(argumentAfter(command, 5), tasks, true);
            case UNMARK -> updateTaskStatus(argumentAfter(command, 7), tasks, false);
            case DELETE -> deleteTask(argumentAfter(command, 7), tasks);
            case TODO -> {
                String description = argumentAfter(command, 5);
                requireNonEmpty(description, "Give the todo a description after 'todo'.");
                addTask(tasks, new Todo(description));
            }
            case DEADLINE -> addDeadline(command, tasks);
            case EVENT -> addEvent(command, tasks);
            case UNKNOWN -> throw new NimbusException("I don't recognise that command.");
        }
    }

    private static String argumentAfter(String command, int startIndex) {
        return command.length() > startIndex ? command.substring(startIndex).trim() : "";
    }

    private static void showTasks(ArrayList<Task> tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i));
        }
    }

    private static void updateTaskStatus(String argument, ArrayList<Task> tasks, boolean isDone)
            throws NimbusException {
        Task task = getTask(argument, tasks);
        if (isDone) {
            task.markAsDone();
            System.out.println("Nice! I've marked this task as done:");
        } else {
            task.markAsNotDone();
            System.out.println("OK, I've marked this task as not done yet:");
        }
        System.out.println("  " + task);
    }

    private static Task getTask(String argument, ArrayList<Task> tasks) throws NimbusException {
        try {
            int taskNumber = Integer.parseInt(argument.trim());
            if (taskNumber < 1 || taskNumber > tasks.size()) {
                throw new NimbusException("Choose a task number from the list.");
            }
            return tasks.get(taskNumber - 1);
        } catch (NumberFormatException e) {
            throw new NimbusException("Enter a valid task number.");
        }
    }

    private static void deleteTask(String argument, ArrayList<Task> tasks) throws NimbusException {
        Task task = getTask(argument, tasks);
        tasks.remove(task);
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    private static void addDeadline(String command, ArrayList<Task> tasks) throws NimbusException {
        int delimiterIndex = command.indexOf(" /by ");
        if (delimiterIndex < 0) {
            throw new NimbusException("Use: deadline DESCRIPTION /by DATE.");
        }
        String description = command.substring(9, delimiterIndex).trim();
        String by = command.substring(delimiterIndex + 5).trim();
        requireNonEmpty(description, "Give the deadline a description.");
        requireNonEmpty(by, "Give the deadline a date after '/by'.");
        addTask(tasks, new Deadline(description, by));
    }

    private static void addEvent(String command, ArrayList<Task> tasks) throws NimbusException {
        int fromIndex = command.indexOf(" /from ");
        int toIndex = command.indexOf(" /to ");
        if (fromIndex < 0 || toIndex < 0 || toIndex <= fromIndex) {
            throw new NimbusException("Use: event DESCRIPTION /from START /to END.");
        }
        String description = command.substring(6, fromIndex).trim();
        String from = command.substring(fromIndex + 7, toIndex).trim();
        String to = command.substring(toIndex + 5).trim();
        requireNonEmpty(description, "Give the event a description.");
        requireNonEmpty(from, "Give the event a start after '/from'.");
        requireNonEmpty(to, "Give the event an end after '/to'.");
        addTask(tasks, new Event(description, from, to));
    }

    private static void requireNonEmpty(String value, String message) throws NimbusException {
        if (value.isEmpty()) {
            throw new NimbusException(message);
        }
    }

    private static void addTask(ArrayList<Task> tasks, Task task) {
        tasks.add(task);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
}
