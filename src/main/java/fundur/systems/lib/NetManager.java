package fundur.systems.lib;

import fundur.systems.lib.sec.EncrState;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static fundur.systems.lib.FileManager.getEncrStateFromJson;
import static fundur.systems.lib.FileManager.loadFile;
import static fundur.systems.lib.sec.Security.*;

public class NetManager {
    public static final String exampleURL = "https://example.org";
    public static String serverURL = "http://127.0.0.1:8000/"; //best server imo

    public static void setServerURL (String newUrl) {
        serverURL = newUrl;
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

}
