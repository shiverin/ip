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
    private static final String WELCOME_MESSAGE = "Hello! I'm Nimbus, your pocket-sized patch of calm.\n"
            + "What shall we clear from your sky today?";
    private static final String LOAD_WARNING = "A little fog rolled in while I loaded your saved tasks. "
            + "We'll start with a clear list.";

    private final Parser parser;
    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;
    private final String startupWarning;

    /** Creates Nimbus with storage at the supplied relative file path. */
    public Nimbus(Path filePath) {
        assert filePath != null : "Storage path must not be null";
        parser = new Parser();
        storage = new Storage(filePath);
        ui = new Ui();
        TaskList loadedTasks;
        String loadingWarning = null;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (IOException e) {
            loadedTasks = new TaskList();
            loadingWarning = LOAD_WARNING;
        }
        tasks = loadedTasks;
        startupWarning = loadingWarning;
    }

    /** Runs the command loop until the user exits or input ends. */
    public void run() {
        ui.showLine();
        ui.show(getWelcomeMessage());
        ui.showLine();
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            Response response = getResponseWithStatus(command);
            ui.show(response.message());
            if (response.isExit()) {
                break;
            }
            ui.showLine();
        }
        ui.close();
    }

    /** Returns Nimbus's response to a command and persists any resulting task changes. */
    public String getResponse(String input) {
        return getResponseWithStatus(input).message();
    }

    /** Returns Nimbus's response and whether the command ends the session. */
    public Response getResponseWithStatus(String input) {
        assert input != null : "Command input must not be null";
        ParsedCommand command = parser.parse(input);
        if (command.type() == CommandType.BYE) {
            return new Response("The sky is clear for now. See you next time!", true);
        }
        try {
            String message = execute(command);
            storage.save(tasks.asList());
            return new Response(message, false);
        } catch (NimbusException e) {
            return new Response("A little turbulence: " + e.getMessage(), false);
        } catch (IOException e) {
            return new Response("I couldn't secure your tasks in the cloud bank: " + e.getMessage(), false);
        }
    }

    /** Returns the greeting and any warning produced while loading saved tasks. */
    public String getWelcomeMessage() {
        return startupWarning == null
                ? WELCOME_MESSAGE
                : WELCOME_MESSAGE + "\n" + startupWarning;
    }

    private String execute(ParsedCommand command) throws NimbusException {
        return switch (command.type()) {
            case LIST -> formatTasks("Here's your current forecast:", tasks.asList());
            case MARK -> markTaskAsDone(command.argument());
            case UNMARK -> markTaskAsNotDone(command.argument());
            case DELETE -> deleteTask(command.argument());
            case UPDATE -> updateTask(command.argument());
            case TODO -> addTodo(command.argument());
            case DEADLINE -> addDeadline(command.argument());
            case EVENT -> addEvent(command.argument());
            case FIND -> findTasks(command.argument());
            case UNKNOWN -> throw new NimbusException("That command drifted past me. Try one from the hint below.");
            case BYE -> throw new IllegalStateException("Bye must be handled before command execution");
        };
    }

    private String markTaskAsDone(String argument) throws NimbusException {
        Task task = tasks.get(parseTaskNumber(argument));
        task.markAsDone();
        return "One cloud cleared! I've marked this task as done:\n  " + task;
    }

    private String markTaskAsNotDone(String argument) throws NimbusException {
        Task task = tasks.get(parseTaskNumber(argument));
        task.markAsNotDone();
        return "No worries—this task is back on the horizon:\n  " + task;
    }

    private String deleteTask(String argument) throws NimbusException {
        Task task = tasks.delete(parseTaskNumber(argument));
        return "Consider it blown away. I've removed this task:\n  " + task
                + "\nYour sky now holds " + tasks.size() + " tasks.";
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
        return "Fresh forecast! I've updated this task:\n  " + task;
    }

    private String addTodo(String description) throws NimbusException {
        requireNonEmpty(description, "Give the todo a description after 'todo'.");
        return addTask(new Todo(description));
    }

    private String addDeadline(String arguments) throws NimbusException {
        String byMarker = " /by ";
        int delimiterIndex = arguments.indexOf(byMarker);
        if (delimiterIndex < 0) {
            throw new NimbusException("Use: deadline DESCRIPTION /by YYYY-MM-DD.");
        }
        String description = arguments.substring(0, delimiterIndex).trim();
        String by = arguments.substring(delimiterIndex + byMarker.length()).trim();
        requireNonEmpty(description, "Give the deadline a description.");
        requireNonEmpty(by, "Give the deadline a date after '/by'.");
        try {
            return addTask(new Deadline(description, by));
        } catch (DateTimeParseException e) {
            throw new NimbusException("Use a deadline date in YYYY-MM-DD format.");
        }
    }

    private String addEvent(String arguments) throws NimbusException {
        String fromMarker = " /from ";
        String toMarker = " /to ";
        int fromIndex = arguments.indexOf(fromMarker);
        int toIndex = arguments.indexOf(toMarker);
        if (fromIndex < 0 || toIndex < 0 || toIndex <= fromIndex) {
            throw new NimbusException("Use: event DESCRIPTION /from START /to END.");
        }
        String description = arguments.substring(0, fromIndex).trim();
        String from = arguments.substring(fromIndex + fromMarker.length(), toIndex).trim();
        String to = arguments.substring(toIndex + toMarker.length()).trim();
        requireNonEmpty(description, "Give the event a description.");
        requireNonEmpty(from, "Give the event a start after '/from'.");
        requireNonEmpty(to, "Give the event an end after '/to'.");
        return addTask(new Event(description, from, to));
    }

    private String findTasks(String keyword) throws NimbusException {
        requireNonEmpty(keyword, "Give me a keyword to find.");
        return formatTasks("These tasks match your search:", tasks.find(keyword));
    }

    private String addTask(Task task) {
        assert task != null : "Task to add must not be null";
        tasks.add(task);
        return "A new cloud is on the horizon. I've added this task:\n  " + task
                + "\nYour sky now holds " + tasks.size() + " tasks.";
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

    /** Contains a command response and its session-exit state. */
    public record Response(String message, boolean isExit) {
    }

    /** Starts Nimbus using its default data file. */
    public static void main(String[] args) {
        new Nimbus(Path.of("data", "nimbus.txt")).run();
    }
}
