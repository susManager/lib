package fundur.systems.lib;

import fundur.systems.lib.local.LocalManager;
import fundur.systems.lib.sec.EncrState;
import fundur.systems.lib.server.ServerManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static fundur.systems.lib.sec.Security.*;

public class Manager {
    private final ExceptionHandler handler;
    private boolean io, net;
    private BaseManager manager;
    private String path;

    public Manager (ExceptionHandler handler) {
        this.handler = handler;
        this.path = getPath();
        updateState();
    }

    public static String getPath() {
        String sep = System.getProperty("file.separator");
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            return "%appdata%" + sep + "susManager" + sep;
        } else {
            return System.getProperty("user.home") + sep + ".config" + sep;
        }
    }

    public void updateState() {
        testIO();
        testNet();
        net = false; // due to server being implemented later on
        manager = net ? new ServerManager() : new LocalManager();
    }

    public boolean testIO() {
        io = true;
        return true;
    }

    public boolean testNet() {
        try {
            URL url = new URL("https://example.org");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            io = true;
            return true;
        } catch (MalformedURLException ignored) {}
          catch (IOException e) {
            handler.handleException(e);
        }
        net = false;
        return false;
    }

    public JSONObject getJSONObject(String hashUser, String password) throws Exception {
        try {
            String file = loadFile("config.json");
            EncrState state = getState(new JSONObject(file).getJSONObject(hashUser));
            String content = loadFile("testFile");
            return new JSONObject(decrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
        } catch (Exception e) {
            handler.handleException(e);
            throw e; //weil hier das error handling echt doppelt Sinn macht
        }
    }

    public boolean saveJSONObject (JSONObject jsonObject, String hashUser, String password) {
        try {
            String content = jsonObject.toString();
            JSONObject config = new JSONObject(loadFile("config.json")).getJSONObject(hashUser);
            EncrState state = getState(config);
            String encrypted = encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
        } catch (Exception e) {
            handler.handleException(e);
        }
        return false;
    }

    public static EncrState getState(JSONObject object) {
        return new EncrState(
            object.getString("algo"),
            serer(object.getJSONArray("salt")),
            serer(object.getJSONArray("iv"))
        );
    }

    public static String loadFile(String path) throws FileNotFoundException {
        File f = new File(path);
        Scanner scanner = new Scanner(f);
        StringBuilder res = new StringBuilder();
        scanner.forEachRemaining(res::append);
        return res.toString();
    }

    public static byte[] serer(JSONArray arr) {
        List<Byte> byteList = new ArrayList<>();
        int temp;
        for (Object o : arr) {
            temp = (Integer) o;
            byteList.add((byte) temp);
        }
        byte[] res = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            res[i] = byteList.get(i);
        }
        return res;
    }

    public static void main(String[] args) {
        try {
            System.out.println(new Manager(null).getJSONObject(hash("fridolin"), "iHaveAids69"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
