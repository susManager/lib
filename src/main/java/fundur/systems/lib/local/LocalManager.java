package fundur.systems.lib.local;

import fundur.systems.lib.BaseManager;

public class LocalManager implements BaseManager {
    public LocalManager() {
    }

    public String getRaw() {
        return "{\"name\": \"fridolin\"}";
    }

    public boolean writeRaw() {
        return false;
    }

    @Override
    public boolean updateRaw() {
        return false;
    }
}
