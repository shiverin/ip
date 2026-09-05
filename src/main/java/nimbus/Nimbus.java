package nimbus;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nimbus.parser.CommandType;
import nimbus.parser.ParsedCommand;
import nimbus.parser.Parser;
import nimbus.storage.Storage;
import nimbus.task.Deadline;
import nimbus.task.Event;
import nimbus.task.Task;
import nimbus.task.TaskList;
import nimbus.task.Todo;
import nimbus.ui.Ui;

/** Runs Nimbus, a personal task assistant. */
public class Nimbus {
    private final Parser parser;
    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;

    /** Creates Nimbus with storage at the supplied relative file path. */
    public Nimbus(Path filePath) {
        assert filePath != null : "Storage path must not be null";
        parser = new Parser();
        storage = new Storage(filePath);
        ui = new Ui();
        TaskList loadedTasks;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (IOException e) {
            loadedTasks = new TaskList();
            ui.show("I couldn't load saved tasks, so we'll start with an empty list.");
        }
        tasks = loadedTasks;
    }

    /** Runs the command loop until the user exits or input ends. */
    public void run() {
        ui.showWelcome();
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.show(getResponse(command));
            if (parser.parse(command).type() == CommandType.BYE) {
                break;
            }
            ui.showLine();
        }
        ui.close();
    }

    /** Returns Nimbus's response to a command and persists any resulting task changes. */
    public String getResponse(String input) {
        assert input != null : "Command input must not be null";
        ParsedCommand command = parser.parse(input);
        if (command.type() == CommandType.BYE) {
            return "Bye. Hope to see you again soon!";
        }
        try {
            String response = execute(command);
            storage.save(tasks.asList());
            return response;
        } catch (NimbusException e) {
            return "I couldn't do that: " + e.getMessage();
        } catch (IOException e) {
            return "I couldn't save your tasks: " + e.getMessage();
        }
    }

    private String execute(ParsedCommand command) throws NimbusException {
        return switch (command.type()) {
            case LIST -> formatTasks("Here are the tasks in your list:", tasks.asList());
            case MARK -> updateTaskStatus(command.argument(), true);
            case UNMARK -> updateTaskStatus(command.argument(), false);
            case DELETE -> deleteTask(command.argument());
            case UPDATE -> updateTask(command.argument());
            case TODO -> addTodo(command.argument());
            case DEADLINE -> addDeadline(command.fullText());
            case EVENT -> addEvent(command.fullText());
            case FIND -> findTasks(command.argument());
            case UNKNOWN -> throw new NimbusException("I don't recognise that command.");
            case BYE -> throw new IllegalStateException("Bye must be handled before command execution");
        };
    }

    private String updateTaskStatus(String argument, boolean isDone) throws NimbusException {
        Task task = tasks.get(parseTaskNumber(argument));
        if (isDone) {
            task.markAsDone();
            return "Nice! I've marked this task as done:\n  " + task;
        } else {
            task.markAsNotDone();
            return "OK, I've marked this task as not done yet:\n  " + task;
        }
    }

    private String deleteTask(String argument) throws NimbusException {
        Task task = tasks.delete(parseTaskNumber(argument));
        return "Noted. I've removed this task:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    private String updateTask(String argument) throws NimbusException {
        int separatorIndex = argument.indexOf(' ');
        if (separatorIndex < 0) {
            throw new NimbusException("Use: update TASK_NUMBER NEW_DESCRIPTION.");
        }
        int taskNumber = parseTaskNumber(argument.substring(0, separatorIndex));
        String description = argument.substring(separatorIndex + 1).trim();
        requireNonEmpty(description, "Give the task a new description.");
        Task task = tasks.get(taskNumber);
        task.setDescription(description);
        return "Got it. I've updated this task:\n  " + task;
    }

    private String addTodo(String description) throws NimbusException {
        requireNonEmpty(description, "Give the todo a description after 'todo'.");
        return addTask(new Todo(description));
    }

    private String addDeadline(String fullCommand) throws NimbusException {
        int delimiterIndex = fullCommand.indexOf(" /by ");
        if (delimiterIndex < 0) {
            throw new NimbusException("Use: deadline DESCRIPTION /by YYYY-MM-DD.");
        }
        String description = fullCommand.substring(9, delimiterIndex).trim();
        String by = fullCommand.substring(delimiterIndex + 5).trim();
        requireNonEmpty(description, "Give the deadline a description.");
        requireNonEmpty(by, "Give the deadline a date after '/by'.");
        try {
            return addTask(new Deadline(description, by));
        } catch (DateTimeParseException e) {
            throw new NimbusException("Use a deadline date in YYYY-MM-DD format.");
        }
    }

    private String addEvent(String fullCommand) throws NimbusException {
        int fromIndex = fullCommand.indexOf(" /from ");
        int toIndex = fullCommand.indexOf(" /to ");
        if (fromIndex < 0 || toIndex < 0 || toIndex <= fromIndex) {
            throw new NimbusException("Use: event DESCRIPTION /from START /to END.");
        }
        String description = fullCommand.substring(6, fromIndex).trim();
        String from = fullCommand.substring(fromIndex + 7, toIndex).trim();
        String to = fullCommand.substring(toIndex + 5).trim();
        requireNonEmpty(description, "Give the event a description.");
        requireNonEmpty(from, "Give the event a start after '/from'.");
        requireNonEmpty(to, "Give the event an end after '/to'.");
        return addTask(new Event(description, from, to));
    }

    private String findTasks(String keyword) throws NimbusException {
        requireNonEmpty(keyword, "Give me a keyword to find.");
        return formatTasks("Here are the matching tasks in your list:", tasks.find(keyword));
    }

    private String addTask(Task task) {
        assert task != null : "Task to add must not be null";
        tasks.add(task);
        return "Got it. I've added this task:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    private static String formatTasks(String heading, List<Task> tasks) {
        if (tasks.isEmpty()) {
            return heading;
        }
        String formattedTasks = IntStream.range(0, tasks.size())
                .mapToObj(index -> (index + 1) + ". " + tasks.get(index))
                .collect(Collectors.joining(System.lineSeparator()));
        return heading + System.lineSeparator() + formattedTasks;
    }

    private static int parseTaskNumber(String argument) throws NimbusException {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new NimbusException("Enter a valid task number.");
        }
    }

    private static void requireNonEmpty(String value, String message) throws NimbusException {
        if (value.isEmpty()) {
            throw new NimbusException(message);
        }
    }

    /** Starts Nimbus using its default data file. */
    public static void main(String[] args) {
        new Nimbus(Path.of("data", "nimbus.txt")).run();
    }
}
