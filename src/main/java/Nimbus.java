import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;

/** Runs Nimbus, a personal task assistant. */
public class Nimbus {
    private final Parser parser;
    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;

    /** Creates Nimbus with storage at the supplied relative file path. */
    public Nimbus(Path filePath) {
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
            ParsedCommand command = parser.parse(ui.readCommand());
            if (command.type() == CommandType.BYE) {
                break;
            }
            try {
                execute(command);
                storage.save(tasks.asList());
            } catch (NimbusException e) {
                ui.showError(e.getMessage());
            } catch (IOException e) {
                ui.showError("I couldn't save your tasks: " + e.getMessage());
            }
            ui.showLine();
        }
        ui.showGoodbye();
        ui.close();
    }

    private void execute(ParsedCommand command) throws NimbusException {
        switch (command.type()) {
            case LIST -> ui.showTasks("Here are the tasks in your list:", tasks.asList());
            case MARK -> updateTaskStatus(command.argument(), true);
            case UNMARK -> updateTaskStatus(command.argument(), false);
            case DELETE -> deleteTask(command.argument());
            case TODO -> addTodo(command.argument());
            case DEADLINE -> addDeadline(command.fullText());
            case EVENT -> addEvent(command.fullText());
            case UNKNOWN -> throw new NimbusException("I don't recognise that command.");
            case BYE -> throw new IllegalStateException("Bye must be handled by the command loop");
        }
    }

    private void updateTaskStatus(String argument, boolean isDone) throws NimbusException {
        Task task = tasks.get(parseTaskNumber(argument));
        if (isDone) {
            task.markAsDone();
            ui.show("Nice! I've marked this task as done:");
        } else {
            task.markAsNotDone();
            ui.show("OK, I've marked this task as not done yet:");
        }
        ui.show("  " + task);
    }

    private void deleteTask(String argument) throws NimbusException {
        Task task = tasks.delete(parseTaskNumber(argument));
        ui.show("Noted. I've removed this task:");
        ui.show("  " + task);
        ui.show("Now you have " + tasks.size() + " tasks in the list.");
    }

    private void addTodo(String description) throws NimbusException {
        requireNonEmpty(description, "Give the todo a description after 'todo'.");
        addTask(new Todo(description));
    }

    private void addDeadline(String fullCommand) throws NimbusException {
        int delimiterIndex = fullCommand.indexOf(" /by ");
        if (delimiterIndex < 0) {
            throw new NimbusException("Use: deadline DESCRIPTION /by YYYY-MM-DD.");
        }
        String description = fullCommand.substring(9, delimiterIndex).trim();
        String by = fullCommand.substring(delimiterIndex + 5).trim();
        requireNonEmpty(description, "Give the deadline a description.");
        requireNonEmpty(by, "Give the deadline a date after '/by'.");
        try {
            addTask(new Deadline(description, by));
        } catch (DateTimeParseException e) {
            throw new NimbusException("Use a deadline date in YYYY-MM-DD format.");
        }
    }

    private void addEvent(String fullCommand) throws NimbusException {
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
        addTask(new Event(description, from, to));
    }

    private void addTask(Task task) {
        tasks.add(task);
        ui.show("Got it. I've added this task:");
        ui.show("  " + task);
        ui.show("Now you have " + tasks.size() + " tasks in the list.");
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
