package fundur.systems.lib;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.*;

import static fundur.systems.lib.Dummy.getDefaultDummyJSON;
import static fundur.systems.lib.Dummy.getNewerDummyJSON;
import static fundur.systems.lib.FileManager.getJSONObject;
import static fundur.systems.lib.FileManager.saveJSONObject;
import static fundur.systems.lib.sec.Security.*;

public class Manager {
    public static final String URL = "https://example.org";

    public static boolean testIO() {
        return true;
    }

    public static boolean testNet() {
        try {
            URL url = new URL(URL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static List<Entry> merge(JSONObject a, JSONObject b) {
        JSONObject jsonA = a.getJSONObject("passwords");
        var iterA = jsonA.keys();
        Map<String, Entry> map = new HashMap<>();

        JSONObject jsonB = b.getJSONObject("passwords");
        var iterB = jsonB.keys();
        while (iterB.hasNext()) {
            String key = iterB.next();
            JSONObject curr = jsonB.getJSONObject(key);
            map.put(key, new Entry(key,
                    curr.getString("usr"),
                    curr.getString("pwd"),
                    curr.getLong("timestamp")));
        }

        while (iterA.hasNext()) {
            String key = iterA.next();
            JSONObject curr = jsonA.getJSONObject(key);

            if (!map.containsKey(key) || curr.getLong("timestamp") > map.get(key).timestamp()) {
                map.put(key, new Entry(key,
                        curr.getString("usr"),
                        curr.getString("pwd"),
                        curr.getLong("timestamp")));
            }
        }
        return new ArrayList<>(map.values());
    }

    @Deprecated
    public static JSONObject getLatestFromServer() {
        return getNewerDummyJSON();
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
        List<Entry> list = merge(getNewerDummyJSON(), getDefaultDummyJSON());
        System.out.println(list.get(0).toString());
    }
}
