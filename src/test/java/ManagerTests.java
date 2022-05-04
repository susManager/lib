import fundur.systems.lib.Entry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fundur.systems.lib.Dummy.getDefaultDummyJSON;
import static fundur.systems.lib.Dummy.getNewerDummyJSON;
import static fundur.systems.lib.Manager.*;
import static org.junit.jupiter.api.Assertions.*;

public class ManagerTests {

    @Test
    public void testMerge() {
        List<Entry> list = merge(getNewerDummyJSON(), getDefaultDummyJSON());
        assertEquals("NEWcockAndBall", list.get(0).pwd());
    }

}
