package utils;

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
}
