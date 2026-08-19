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
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                break;
            }
            if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ". " + tasks.get(i));
                }
            } else if (command.startsWith("mark ")) {
                int taskNumber = Integer.parseInt(command.substring(5));
                Task task = tasks.get(taskNumber - 1);
                task.markAsDone();
                System.out.println("Nice! I've marked this task as done:");
                System.out.println("  " + task);
            } else if (command.startsWith("unmark ")) {
                int taskNumber = Integer.parseInt(command.substring(7));
                Task task = tasks.get(taskNumber - 1);
                task.markAsNotDone();
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println("  " + task);
            } else if (command.startsWith("todo ")) {
                addTask(tasks, new Todo(command.substring(5)));
            } else if (command.startsWith("deadline ")) {
                int delimiterIndex = command.indexOf(" /by ");
                String description = command.substring(9, delimiterIndex);
                String by = command.substring(delimiterIndex + 5);
                addTask(tasks, new Deadline(description, by));
            } else if (command.startsWith("event ")) {
                int fromIndex = command.indexOf(" /from ");
                int toIndex = command.indexOf(" /to ");
                String description = command.substring(6, fromIndex);
                String from = command.substring(fromIndex + 7, toIndex);
                String to = command.substring(toIndex + 5);
                addTask(tasks, new Event(description, from, to));
            } else {
                addTask(tasks, new Todo(command));
            }
            System.out.println(DIVIDER);
        }
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
        scanner.close();
    }

    private static void addTask(ArrayList<Task> tasks, Task task) {
        tasks.add(task);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
}
