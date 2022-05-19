package fundur.systems.lib;

import fundur.systems.lib.sec.EncrState;
import fundur.systems.lib.sec.Security;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static fundur.systems.lib.Dummy.getDefaultDummyJSON;
import static fundur.systems.lib.FileManager.*;
import static fundur.systems.lib.NetManager.*;
import static fundur.systems.lib.sec.Security.*;

public class Manager {
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

    public static String encrypt(JSONObject jsonObject, String path, String hashUser, String password ) throws FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String content = jsonObject.toString();
        JSONObject config = new JSONObject(loadFile(path + "config.json")).getJSONObject(hashUser);
        EncrState state = getEncrStateFromJson(config);
        return Security.encrypt(state.algo(), content, getKeyFromPwd(password, state.salt()), new IvParameterSpec(state.iv()));
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
                "fridolin",
                "iHaveAids69",
                ""
            );

            System.out.println(getJSONObjectFromFile(hash("fridolin"), "iHaveAids69", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        postLatestToServer(hash("fridolin"), Manager.encrypt(getDefaultDummyJSON(), "", hash("fridolin"), "iHaveAids69"));
        String file = loadFile("config.json");
        postEncrStateToServer(hash("fridolin"), getEncrStateFromJson(new JSONObject(file).getJSONObject(hash("fridolin"))).toString());
    }
}
