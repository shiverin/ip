package nimbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NimbusTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void getResponse_updateCommand_updatesExistingTask() {
        Nimbus nimbus = new Nimbus(temporaryDirectory.resolve("tasks.txt"));
        nimbus.getResponse("todo old description");

        String response = nimbus.getResponse("update 1 new description");

        assertTrue(response.contains("[T][ ] new description"));
        assertTrue(nimbus.getResponse("list").contains("[T][ ] new description"));
    }

    @Test
    void getResponse_deadlineWithoutDescription_returnsUsageError() {
        Nimbus nimbus = new Nimbus(temporaryDirectory.resolve("tasks.txt"));

        String response = nimbus.getResponse("deadline /by 2026-01-01");

        assertEquals("I couldn't do that: Use: deadline DESCRIPTION /by YYYY-MM-DD.", response);
    }

    @Test
    void getResponse_eventWithoutDescription_returnsUsageError() {
        Nimbus nimbus = new Nimbus(temporaryDirectory.resolve("tasks.txt"));

        String response = nimbus.getResponse("event /from Monday /to Tuesday");

        assertEquals("I couldn't do that: Use: event DESCRIPTION /from START /to END.", response);
    }
}
