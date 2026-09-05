package nimbus;

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
}
