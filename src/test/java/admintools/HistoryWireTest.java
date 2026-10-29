package admintools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoryWireTest {
    @Test
    void readsServerContractWithWideValues() {
        String json = "{\"version\":2,\"requestId\":7,\"sessionId\":\"map\",\"nextCursor\":\"\",\"query\":"
                      + "\"tile\",\"truncated\":false,\"entries\":[{\"x\":1,\"y\":2,\"block\":2048,"
                      + "\"rotation\":0,\"config_type\":8,\"action_kind\":6,\"name\":\"Builder\",\"uuid\":"
                      + "\"u\",\"config\":\"copper\",\"action\":\"Configure\",\"valid\":true,\"destroy\":"
                      + "false,\"legacy\":false,\"time\":70000,\"sequence\":4096}]}";
        var page = HistoryWire.read(HistoryPage.class, json);
        assertEquals(2, page.version);
        assertEquals(70000, page.entries[0].time);
        assertEquals(2048, page.entries[0].block);
        assertTrue(HistoryWire.write(page).contains("\"version\":2"));
    }
    @Test
    void typeHintsAreNotInterpreted() {
        assertThrows(RuntimeException.class,
                () -> HistoryWire.read(HistoryPage.class, "{\"class\":\"java.lang.Thread\"}"));
    }
}
