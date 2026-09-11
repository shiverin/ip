package nimbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
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

        assertEquals("A little turbulence: Use: deadline DESCRIPTION /by YYYY-MM-DD.", response);
    }

    @Test
    void getResponse_eventWithoutDescription_returnsUsageError() {
        Nimbus nimbus = new Nimbus(temporaryDirectory.resolve("tasks.txt"));

        String response = nimbus.getResponse("event /from Monday /to Tuesday");

        assertEquals("A little turbulence: Use: event DESCRIPTION /from START /to END.", response);
    }

    @Test
    void getResponseWithStatus_byeWithTrailingText_marksSessionForExit() {
        Nimbus nimbus = new Nimbus(temporaryDirectory.resolve("tasks.txt"));

        Nimbus.Response response = nimbus.getResponseWithStatus("bye now");

        assertTrue(response.isExit());
        assertEquals("The sky is clear for now. See you next time!", response.message());
    }

    @Test
    void getResponseWithStatus_regularCommand_keepsSessionOpen() {
        Nimbus nimbus = new Nimbus(temporaryDirectory.resolve("tasks.txt"));

        Nimbus.Response response = nimbus.getResponseWithStatus("todo read book");

        assertFalse(response.isExit());
    }

    @Test
    void getWelcomeMessage_storageLoadFails_includesWarning() throws IOException {
        Path blockingFile = temporaryDirectory.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");

        Nimbus nimbus = new Nimbus(blockingFile.resolve("tasks.txt"));

        assertTrue(nimbus.getWelcomeMessage().contains("fog rolled in"));
    }
}
