package fundur.systems.lib;

import fundur.systems.lib.sec.EncrState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.spec.IvParameterSpec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static fundur.systems.lib.sec.Security.*;
import static fundur.systems.lib.sec.Security.getKeyFromPwd;

public class FileManager {
    public static boolean testIO() {
        return true;
    }
    public static @NotNull JSONObject getJSONObjectFromFile(String hashUser, String password, String path) throws Exception {
        String file = loadFile(path + "config.json");
        EncrState state = getEncrStateFromJson(new JSONObject(file).getJSONObject(hashUser));
        String content = loadFile(path + hashUser);
        return new JSONObject(decrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
    }

    public static void saveJSONObjectToFile(@NotNull JSONObject jsonObject, String hashUser, String password, String path) throws Exception {
        String content = jsonObject.toString();
        JSONObject config = new JSONObject(loadFile(path + "config.json")).getJSONObject(hashUser);
        EncrState state = getEncrStateFromJson(config);
        String encrypted = encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
        saveFile(encrypted, path + hashUser);
    }

    @Contract("_ -> new")
    public static @NotNull EncrState getEncrStateFromJson(@NotNull JSONObject object) {
        return new EncrState(
                object.getString("algo"),
                JSONArrayToByteArray(object.getJSONArray("salt")),
                JSONArrayToByteArray(object.getJSONArray("iv"))
        );
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

    public static byte @NotNull [] JSONArrayToByteArray(@NotNull JSONArray arr) {
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
}
