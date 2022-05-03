package fundur.systems.lib;

import fundur.systems.lib.sec.EncrState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static fundur.systems.lib.sec.Security.*;

public class Manager {
    public static @NotNull String getDefaultPath() {
        String sep = System.getProperty("file.separator");
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            return "%appdata%" + sep + "susManager" + sep;
        } else {
            return System.getProperty("user.home") + sep + ".config" + sep;
        }
    }

    public static boolean testIO() {
        return true;
    }

    public static boolean testNet() {
        try {
            URL url = new URL("https://example.org");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static @NotNull JSONObject getJSONObject(String hashUser, String password, String path) throws Exception {
        String file = loadFile(path + "config.json");
        EncrState state = getState(new JSONObject(file).getJSONObject(hashUser));
        String content = loadFile(path + hashUser);
        return new JSONObject(decrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
    }

    public static void saveJSONObject (@NotNull JSONObject jsonObject, String hashUser, String password, String path) throws Exception {
        String content = jsonObject.toString();
        JSONObject config = new JSONObject(loadFile(path + "config.json")).getJSONObject(hashUser);
        EncrState state = getState(config);
        String encrypted = encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
        saveFile(encrypted, path + hashUser);
    }

    @Contract("_ -> new")
    public static @NotNull EncrState getState(@NotNull JSONObject object) {
        return new EncrState(
            object.getString("algo"),
            serer(object.getJSONArray("salt")),
            serer(object.getJSONArray("iv"))
        );
    }

    @Deprecated
    public static JSONObject getLatestFromServer() {
        return getNewerDummyJSON();
    }

    public static void saveFile(String content, String path) throws IOException {
        FileWriter f = new FileWriter(path);
        f.write(content);
        f.close();
    }

    public static @NotNull String loadFile(String path) throws FileNotFoundException {
        File f = new File(path);
        Scanner scanner = new Scanner(f);
        StringBuilder res = new StringBuilder();
        scanner.forEachRemaining(res::append);
        return res.toString();
    }

    public static byte @NotNull [] serer(@NotNull JSONArray arr) {
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

    public static JSONObject getDefaultDummyJSON() {
        return new JSONObject("""
                        { "passwords": {
                            "gmail": {
                                "name": "gmail",
                                "usr": "cockUser@gmail.com",
                                "pwd": "cockAndBall",
                                "timestamp": 1651574125604
                            }
                          }
                        }
                        """);
    }

    public static JSONObject getNewerDummyJSON() {
        return new JSONObject("""
                        { "passwords": {
                            "gmail": {
                                "name": "gmail",
                                "usr": "cockUser@gmail.com",
                                "pwd": "cockAndBall",
                                "timestamp": 1651574187604
                            }
                          }
                        }
                        """);
    }

    public static void main(String[] args) {
        try {
            saveJSONObject(
                getDefaultDummyJSON(),
                hash("fridolin"),
                "iHaveAids69",
                ""
            );

            System.out.println(getJSONObject(hash("fridolin"), "iHaveAids69", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
