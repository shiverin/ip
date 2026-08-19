/** Represents a task that must be completed by a deadline. */
public class Deadline extends Task {
    protected final String by;

    /** Creates a deadline task. */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
