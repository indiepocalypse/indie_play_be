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

    public static String get_or_null(JsonNode json, String key) {
        JsonNode subnode = json.get(key);
        if (subnode==null) {
            return null;
        }
        else {
            return subnode.asText();
        }
    }

    public static int get_or_negative(JsonNode json, String key) {
        // TODO: I am very aware this is ugly (inline missing value reporting). Fixing requires having optional types? or a different model_user with partial info?
        JsonNode subnode = json.get(key);
        if (subnode==null) {
            return -1;
        }
        else {
            return subnode.asInt();
        }
    }
}
