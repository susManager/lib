package fundur.systems.lib.sec;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.Stream;

public class EncrState {
    private String algo;
    private byte[] salt;
    private byte[] iv;

    public EncrState(String algo, byte[] salt, byte[] iv) {
        this.algo = algo;
        this.salt = salt;
        this.iv = iv;
    }

    public String algo() {
        return algo;
    }

    public byte[] salt() {
        return salt;
    }

    public byte[] iv() {
        return iv;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("iv", iv);
        json.put("salt", salt);
        json.put("algo", algo);
        return json.toString();
    }

    public JSONObject toJSON () {
        JSONObject json = new JSONObject();
        json.put("algo", algo());
        JSONArray iv = new JSONArray();
        for (byte b : this.iv) {
            iv.put(b);
        }
        json.put("iv", iv);
        JSONArray salt = new JSONArray();
        for (byte b : this.salt) {
            salt.put(b);
        }
        json.put("salt", salt);
        return json;
    }
}
