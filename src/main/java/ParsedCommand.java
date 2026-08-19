/** Contains a recognised command type and the original command text. */
public record ParsedCommand(CommandType type, String fullText) {
    /** Returns the trimmed text following the command keyword. */
    public String argument() {
        int separatorIndex = fullText.indexOf(' ');
        return separatorIndex < 0 ? "" : fullText.substring(separatorIndex + 1).trim();
    }
}
