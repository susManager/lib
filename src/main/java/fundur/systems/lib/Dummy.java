package fundur.systems.lib;

import org.json.JSONObject;

public class Dummy {
    public static JSONObject getDefaultDummyJSON() {
        return new JSONObject("""
                {
                	"passwords": [{
                		"name": "gmail",
                		"usr": "cockUser@gmail.com",
                		"pwd": "cockAndBall",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Discord",
                		"usr": "discord@gmail.com",
                		"pwd": "discordPassowrd",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Steam",
                		"usr": "steamUser@gmail.com",
                		"pwd": "steamPassword",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Teams",
                		"usr": "teamsUser@gmail.com",
                		"pwd": "teamsPassword",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Google",
                		"usr": "googleUser@gmail.com",
                		"pwd": "googlePassword",
                		"timestamp": 1651574125604
                	}
                	]
                }
                        """);
    }

    public static JSONObject getNewerDummyJSON() {
        return new JSONObject("""
                {
                	"passwords": [{
                		"name": "gmail",
                		"usr": "NEWcockUser@gmail.comm",
                		"pwd": "NEWcockAndBall",
                		"timestamp": 1651574187604
                	}]
                }
                        """);
    }
}
