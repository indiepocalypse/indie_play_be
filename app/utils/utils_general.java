package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

/**
 * Created by skariel on 13/10/15.
 */
public class utils_general {
    public static WSClient getwsclient() {
        WSClient ws;
        try {
            ws = WS.client();
        } catch (Exception ignored) {
            ws = WS.newClient(1);
        }
        return ws;
    }

    // TODO: use these two getters everywhere...
    public static String str_or_null(JsonNode json, String key) {
        JsonNode subnode = json.get(key);
        if (subnode==null) {
            return null;
        }
        else {
            return subnode.asText();
        }
    }
    public static Integer int_or_null(JsonNode json, String key) {
        JsonNode subnode = json.get(key);
        if (subnode==null) {
            return null;
        }
        else {
            return subnode.asInt();
        }
    }
}
