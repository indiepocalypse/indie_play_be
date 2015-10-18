package utils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by skariel on 18/10/15.
 */
public class utils_json {
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
    public static Long long_or_null(JsonNode json, String key) {
        JsonNode subnode = json.get(key);
        if (subnode==null) {
            return null;
        }
        else {
            return subnode.asLong();
        }
    }
    public static Boolean false_otherwise(JsonNode json, String key) {
        JsonNode subnode = json.get(key);
        if (subnode==null) {
            return false;
        }
        else {
            if (subnode.asText().equals("true")) {
                return true;
            }
            return false;
        }
    }
}
