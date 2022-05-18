package fundur.systems.lib;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static fundur.systems.lib.Dummy.getDefaultDummyJSON;
import static fundur.systems.lib.Dummy.getNewerDummyJSON;
import static fundur.systems.lib.FileManager.getJSONObjectFromFile;
import static fundur.systems.lib.FileManager.saveJSONObjectToFile;
import static fundur.systems.lib.sec.Security.*;

public class Manager {
    public static final String exampleURL = "https://example.org";
    public static String serverURL = "http://susmanager.fundur.systems:1337/";

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

    public static JSONObject getLatestFromServer(String nameHash) throws IOException {
        URL server = new URL("http://susmanager.fundur.systems:1337/data/" + nameHash);
        HttpURLConnection conn = (HttpURLConnection) server.openConnection();
        conn.setRequestMethod("GET");
        StringBuilder output = new StringBuilder();
        var br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String line = "";
        while ((line = br.readLine()) != null  )
            output.append(line);

        return new JSONObject(output.toString());
    }

    public static void postLatestToServer (String nameHash, List<Entry> list) {

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

    public static void main(String[] args) {
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
        List<Entry> list = merge(getNewerDummyJSON(), getDefaultDummyJSON());
        System.out.println(list.get(0).toString());
        System.out.println(list2JSONObject(merge(getNewerDummyJSON(), getDefaultDummyJSON())));
    }
}
