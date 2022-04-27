package fundur.systems.lib.server;

import fundur.systems.lib.BaseManager;

public class ServerManager implements BaseManager {
    public static boolean working() {
        return false;
    }

    @Override
    public String getRaw() {
        return null;
    }

    @Override
    public boolean updateRaw() {
        return false;
    }

    @Override
    public boolean writeRaw() {
        return false;
    }
}
