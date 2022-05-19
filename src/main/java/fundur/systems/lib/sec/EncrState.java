package fundur.systems.lib.sec;

import org.json.JSONObject;

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
}
