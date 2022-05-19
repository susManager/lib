package fundur.systems.lib;

import fundur.systems.lib.sec.EncrState;
import fundur.systems.lib.sec.Security;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static fundur.systems.lib.Dummy.getDefaultDummyJSON;
import static fundur.systems.lib.FileManager.*;
import static fundur.systems.lib.sec.Security.*;

public class Manager {
    public static final String exampleURL = "https://example.org";
    public static String serverURL = "http://127.0.0.1:8000/"; //best server imo

    public static void setServerURL (String newUrl) {
        serverURL = newUrl;
    }

    public static boolean testIO() {
        return true;
    }

    public static boolean testNet() {
        try {
            URL url = new URL(exampleURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static List<Entry> merge(JSONObject a, JSONObject b) {
        Map<String, Entry> map = new HashMap<>();

        JSONArray jsonA = a.getJSONArray("passwords");
        for (int i = 0; i < jsonA.length(); i++) {
            JSONObject curr = (JSONObject) jsonA.get(i);
            var key = curr.getString("name");
            map.put(key, new Entry(key,
                    curr.getString("usr"),
                    curr.getString("pwd"),
                    curr.getLong("timestamp")));
        }

        JSONArray jsonB = b.getJSONArray("passwords");
        for (int i = 0; i < jsonB.length(); i++) {
            JSONObject curr = (JSONObject) jsonB.get(i);
            var key = curr.getString("name");
            if (!map.containsKey(key) || curr.getLong("timestamp") > map.get(key).timestamp()) {
                map.put(key, new Entry(key,
                        curr.getString("usr"),
                        curr.getString("pwd"),
                        curr.getLong("timestamp")));
            }
        }
        return new ArrayList<>(map.values());
    }

    public static String postEncrStateToServer(String user, String stringified) throws IOException {
        return postRawEncrStateToServer(user, Base64.getEncoder().encodeToString(stringified.getBytes()));
    }
    public static String postRawEncrStateToServer(String user, String encoded) throws IOException {
        String nameHash = hash(user);
        URL server = new URL(serverURL + "encrstate/" +  nameHash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(String.format("""
                    {"content": "%s"}
                    """, encoded).getBytes());
        }


        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String out = br.lines().reduce((x, y) -> x + y).get();

        System.out.println(out);
        return out;
    }

    public static String getEncrStateFromServer(String user) throws IOException {
        String nameHash = hash(user);
        URL server = new URL(serverURL + "encrstate/" +  nameHash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("GET");

        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        return new String(Base64.getDecoder().decode(br.lines().reduce((x, y) -> x + y).get()));
    }

    public static String getLatestRawFromServer(String nameHash) throws IOException {
        URL server = new URL(serverURL + "data/" +  nameHash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("GET");

        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        return br.lines().reduce((x, y) -> x + y).get();
    }

    public static JSONObject getLatestFromServer(String user, String password, String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String hashUser = hash(user);
        String encrypted = getLatestRawFromServer(hashUser);
        String file = loadFile(path + "config.json");
        EncrState state = getEncrStateFromJson(new JSONObject(file).getJSONObject(hashUser));
        return new JSONObject(decrypt(state.algo(), encrypted, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
    }

    public static String encrypt(JSONObject jsonObject, String path, String hashUser, String password ) throws FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String content = jsonObject.toString();
        JSONObject config = new JSONObject(loadFile(path + "config.json")).getJSONObject(hashUser);
        EncrState state = getEncrStateFromJson(config);
        return Security.encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
    }

    public static String postLatestToServer (String user, String encrypted) throws IOException {
        String nameHash = hash(user);
        URL server = new URL(serverURL + "data/" +  nameHash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(String.format("""
                    {"content": "%s"}
                    """, encrypted).getBytes());
        }


        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String out = br.lines().reduce((x, y) -> x + y).get();

        System.out.println(out);
        return out;
    }

    public static JSONObject list2JSONObject (List<Entry> list) {
        JSONArray arr = new JSONArray();
        for (Entry entry : list)
            arr.put(new JSONObject(String.format("""
                    {   "name": "%s",
                        "usr": "%s",
                        "pwd": "%s",
                        "timestamp": %d
                    }
                    """,
                entry.name(), entry.usr(), entry.pwd(), entry.timestamp())));
        JSONObject result = new JSONObject();
        result.put("passwords", arr);
        return result;
    }

    @Deprecated
    public static List<Entry> JSONObject2List(JSONObject json) {
        List<Entry> result = new ArrayList<>();
        JSONArray jsonA = json.getJSONArray("passwords");
        for (int i = 0; i < jsonA.length(); i++) {
            JSONObject curr = (JSONObject) jsonA.get(i);
             result.add(new Entry(curr.getString("name"),
                    curr.getString("usr"),
                    curr.getString("pwd"),
                    curr.getLong("timestamp")));
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        try {
            saveJSONObjectToFile(
                getDefaultDummyJSON(),
                hash("fridolin"),
                "iHaveAids69",
                ""
            );

            System.out.println(getJSONObjectFromFile(hash("fridolin"), "iHaveAids69", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        postLatestToServer("fridolin", Manager.encrypt(getDefaultDummyJSON(), "", hash("fridolin"), "iHaveAids69"));
        System.out.println(getLatestFromServer("fridolin", "iHaveAids69", ""));
        System.out.println(postEncrStateToServer("fridolin", "{\"content\": \"bruder\"}"));
        System.out.println(getEncrStateFromServer("fridolin"));
    }
}
