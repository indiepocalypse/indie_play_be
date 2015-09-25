package controllers;

import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

/**
 * Created by skariel on 21/09/15.
 */
public class github_access {
    static final String client_id = "9bc20dbe14087a22b6a7";
    static final String callback_uri = "http://127.0.0.1:9000/";
    static final String scope = ""; //"user,public_repo";
    public static String get_github_access_url(String state) {
        final String github_access = "https://github.com/login/oauth/authorize?client_id=__CLIENT_ID__&redirect_uri=__CALLBACK_URI__&scope="+scope+"&state=__STATE__";
        return github_access.replace("__STATE__", state)
                .replace("__CLIENT_ID__", client_id)
                .replace("__CALLBACK_URI__", callback_uri);
    }
    public static final WSRequest get_github_access_token(WSClient ws, String state, String code) {
        return ws.url("https://github.com/login/oauth/access_token")
                .setMethod("POST")
                .setQueryParameter("client_id",client_id)
                .setQueryParameter("client_secret", "48dcb176141e10a3ed14942e918b693aea2d6364")
                .setQueryParameter("code", code)
                .setQueryParameter("state", state);
    }
    public static String get_random_string() {
        return new RandomString(12).nextString();
    }

    public static final WSRequest indie_auth_request(WSClient ws, credentials credentials, String path) {
        return ws.url("https://api.github.com"+path)
                .setHeader("Authorization", "Basic "+credentials.getAuth());
    }
    public static final WSRequest get_indie_repositories(WSClient ws, credentials credentials) {
        return indie_auth_request(ws, credentials, "/user")
                .setMethod("GET");
        // TODO: this is just a stub, getting the user details for testing basic auth
    }
}
