package fundur.systems.lib;

import fundur.systems.lib.sec.EncrState;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static fundur.systems.lib.sec.Security.*;
import static fundur.systems.lib.sec.Security.getKeyFromPwd;

public class FileManager {
    public static boolean testIO() {
        File f = new File("config.json");
        return f.canRead() && f.canWrite();
    }
    public static JSONObject getJSONObjectFromFile(String hashUser, String password, String path) throws Exception {
        String file = loadFile(path + "config.json");
        EncrState state = getEncrStateFromJson(new JSONObject(file));
        String content = loadFile(path + hashUser);
        return new JSONObject(decrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
    }

    public static List<Entry> getEntryListFromFile (String password, File encrState, File encrypted) throws FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String encrstate = loadFile(encrState);
        EncrState state = getEncrStateFromJson(new JSONObject(encrstate));
        String content = loadFile(encrypted);
        JSONObject n = new JSONObject(decrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
        return Manager.JSONObject2List(n);
    }

    public static List<Entry> getEntryListFromFile (String password, EncrState state, File encrypted) throws FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String content = loadFile(encrypted);
        JSONObject n = new JSONObject(decrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
        return Manager.JSONObject2List(n);
    }

    public static void saveToFile(List<Entry> entries, EncrState state, File file, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        String content = Manager.list2JSONObject(entries).toString();
        String encrypted = encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
        saveFile(encrypted, file);
    }

    public static void saveEncrStateToFile (EncrState state, File file) throws IOException {
        saveFile(state.toJSON().toString(), file);
    }

    public static EncrState getEncrStateFromFile (String file) throws FileNotFoundException {
        String encrstate = loadFile(file);
        return getEncrStateFromJson(new JSONObject(encrstate));
    }

    public static void saveJSONObjectToFile(JSONObject jsonObject, String user, String password, String path) throws Exception {
        String hashUser = hash(user);
        String content = jsonObject.toString();
        JSONObject config = new JSONObject(loadFile(path + "config.json"));
        EncrState state = getEncrStateFromJson(config);
        String encrypted = encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
        saveFile(encrypted, path + hashUser);
    }

    public static EncrState getEncrStateFromJson(JSONObject object) {
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

    public static void saveFile (String content, File file) throws IOException {
        FileWriter f = new FileWriter(file);
        f.write(content);
        f.close();
    }

    public static String loadFile(File f) throws FileNotFoundException {
        Scanner scanner = new Scanner(f);
        StringBuilder res = new StringBuilder();
        scanner.forEachRemaining(res::append);
        return res.toString();
    }

    public static String loadFile(String path) throws FileNotFoundException {
        File f = new File(path);
        Scanner scanner = new Scanner(f);
        StringBuilder res = new StringBuilder();
        scanner.forEachRemaining(res::append);
        return res.toString();
    }

    public static byte [] JSONArrayToByteArray(JSONArray arr) {
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
