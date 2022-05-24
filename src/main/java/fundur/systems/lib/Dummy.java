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
                		"notes": "gmails notes",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Discord",
                		"usr": "discord@gmail.com",
                		"pwd": "discordPassowrd",
                		"notes": "discord notes",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Steam",
                		"usr": "steamUser@gmail.com",
                		"pwd": "steamPassword",
                		"notes": "steam notes",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Teams",
                		"usr": "teamsUser@gmail.com",
                		"pwd": "teamsPassword",
                		"notes": "teams notes",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Google",
                		"usr": "googleUser@gmail.com",
                		"pwd": "googlePassword",
                		"notes": "google notes",
                		"timestamp": 1651574125604
                	},
                	{
                	    "name": "Leddit",
                		"usr": "ledit@gmail.com",
                		"pwd": "ledit pwd",
                		"notes": "ledit notes",
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
                		"notes": "bruhv",
                		"timestamp": 1651574187604
                	}]
                }
                        """);
    }
}
