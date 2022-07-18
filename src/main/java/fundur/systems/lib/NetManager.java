package fundur.systems.lib;

import fundur.systems.lib.sec.Encoder;
import fundur.systems.lib.sec.EncrState;
import fundur.systems.lib.sec.Security;
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
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static fundur.systems.lib.FileManager.getEncrStateFromJson;
import static fundur.systems.lib.FileManager.loadFile;
import static fundur.systems.lib.sec.Security.*;

public class NetManager {
    public static String serverURL = "http://127.0.0.1:6969/"; //best server imo

    public static void setServerURL (String newUrl) {
        serverURL = newUrl;
    }

    public static boolean testNet() {
        try {
            URL url = new URL(serverURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static String postLatestToServer (String user, String encrypted) throws IOException {
        return postRawToServer(hash(user), "data", encrypted);
    }

    public static String postSignedToServer (String user, String encrypted, String pwd) throws IOException, GeneralSecurityException {
        String hash = hash(user);
        String path = "data";

        URL server = new URL(serverURL + path + "/" +  hash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(String.format("""
                    {"content": "%s",
                    "sign": "%s"}
                    """, encrypted, sign(encrypted, pwd)).getBytes());
        }

        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        return br.lines().reduce((x, y) -> x + y).get();
    }

    public static String postEncrSignToServer(String user, EncrState state, String pwd) throws IOException, GeneralSecurityException {
        String hash = hash(user);
        String path = "encrstate";

        URL server = new URL(serverURL + path + "/" +  hash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(String.format("""
                    {"content": "%s",
                    "sign": "%s"}
                    """,
                    Encoder.encode(state.toString().getBytes()),
                    Encoder.encode(Security.getKeyPair(pwd).getPublic().getEncoded())
            ).getBytes());
        }

        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        return br.lines().reduce((x, y) -> x + y).get();
    }

    public static String postEncrStateToServer(String user, String stringified) throws IOException {
        return postRawToServer(hash(user), "encrstate", Encoder.encode(stringified.getBytes()));
    }

    public static String postRawToServer (String hash, String path, String raw) throws IOException {
        URL server = new URL(serverURL + path + "/" +  hash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(String.format("""
                    {"content": "%s"}
                    """, raw).getBytes());
        }

        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String out = br.lines().reduce((x, y) -> x + y).get();

        return out;
    }

    public static EncrState getEncrStateFromServer(String user) throws IOException {
        return getEncrStateFromJson(new JSONObject(new String(Encoder.decode(getRawFromServer(hash(user), "encrstate")))));
    }

    public static String getLatestRawFromServer(String user) throws IOException {
        return getRawFromServer(user, "data");
    }

    private static String getRawFromServer(String nameHash, String path) throws IOException {
        URL server = new URL(serverURL + path + "/" +  nameHash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("GET");

        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        return br.lines().reduce((x, y) -> x + y).get();
    }

    public static JSONObject getLatestFromServer(String user, String password, String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String file = loadFile(path + "config.json");
        EncrState state = getEncrStateFromJson(new JSONObject(file));
        return getLatestFromServer(user, password, state);
    }

    public static JSONObject getLatestFromServer(String user, String password, EncrState state) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String encrypted = getLatestRawFromServer(hash(user));
        return new JSONObject(decrypt(state.algo(), encrypted, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv())));
    }

    public static boolean exists(String usr) throws IOException {
        return getRawFromServer(hash(usr), "exists").equals("yes");
    }
}
