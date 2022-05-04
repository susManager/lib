package fundur.systems.lib;

import org.json.JSONObject;

public class Dummy {
    public static JSONObject getDefaultDummyJSON() {
        return new JSONObject("""
                        { "passwords": {
                            "gmail": {
                                "name": "gmail",
                                "usr": "cockUser@gmail.com",
                                "pwd": "cockAndBall",
                                "timestamp": 1651574125604
                            }
                          }
                        }
                        """);
    }

    public static JSONObject getNewerDummyJSON() {
        return new JSONObject("""
                        { "passwords": {
                            "gmail": {
                                "name": "gmail",
                                "usr": "NEWcockUser@gmail.com",
                                "pwd": "NEWcockAndBall",
                                "timestamp": 1651574187604
                            }
                          }
                        }
                        """);
    }
}