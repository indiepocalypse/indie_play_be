package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 05/10/15.
 */
public class store {

    final static String user_name_session_key = "user_name";
    final static String avatar_url_session_key = "avatar_url";
    final static String state_session_key = "state";
    final static String returnto_session_key = "returnto";
    final static String token_session_key = "token";
    final static String github_code_session_key = "github_code";
    final static Github_Credentials github_credentials = new Github_Credentials();

    public static boolean user_is_logged(ApplicationRoutes app) {
        return get_token(app) != null;
    }

    private static void fetch_user(ApplicationRoutes app) {
        WSResponse res_user;
        WSRequest req_user = github_access.user_auth_request(app.ws, app.session().get("token"), "/user")
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        String avatar_url = Json.parse(res_user.getBody())
                .get("avatar_url")
                .asText();
        app.session().put(avatar_url_session_key, avatar_url);
        String user_name = Json.parse(res_user.getBody())
                .get("login")
                .asText();
        app.session().put(user_name_session_key, user_name);
    }

    public static String get_avatar_url(ApplicationRoutes app) {
        if (!user_is_logged(app)) {
            return null;
        }
        String url = app.session().get(avatar_url_session_key);
        if (url!=null) {
            return url;
        }
        fetch_user(app);
        return app.session().get(avatar_url_session_key);
    }

    public static String get_user_name(ApplicationRoutes app) {
        if (!user_is_logged(app)) {
            return null;
        }
        String name = app.session().get(user_name_session_key);
        if (name!=null) {
            return name;
        }
        fetch_user(app);
        return app.session().get(user_name_session_key);
    }

    public static String get_state(ApplicationRoutes app) {
        return app.session().get(state_session_key);
    }

    public static void set_state(ApplicationRoutes app, String state) {
        app.session().put(state_session_key, state);
    }

    public static boolean has_returnto(ApplicationRoutes app) {
        return app.session().get(returnto_session_key) != null;
    }

    public static void set_return_to(ApplicationRoutes app, String to) {
        app.session().put(returnto_session_key, to);
    }

    public static String pop_return_to(ApplicationRoutes app) {
        if (!app.session().containsKey(returnto_session_key)) {
            return null;
        }
        String returnto = app.session().get(returnto_session_key);
        app.session().remove(returnto_session_key);
        return returnto;
    }

    public static void set_token(ApplicationRoutes app, String token) {
        app.session().put(token_session_key, token);
    }

    public static String get_token(ApplicationRoutes app) {
        return app.session().get(token_session_key);
    }

    public static void set_github_code(ApplicationRoutes app, String code) {
        app.session().put(github_code_session_key, code);
    }

    public static String get_github_code(ApplicationRoutes app) {
        return app.session().get(github_code_session_key);
    }

    public static void clear(ApplicationRoutes app) {
        app.session().clear();
    }

    public static String get_indie_github_name() {
        return github_credentials.name;
    }
    public static String get_indie_github_pssw() {
        return github_credentials.pssw;
    }
    public static String get_indie_github_auth() {
        return github_credentials.getAuth();
    }
    public static String get_indie_github_client_id() {
        return github_credentials.getClient_id();
    }
    public static String get_indie_github_client_secret() {
        return github_credentials.getClient_secret();
    }

    static class Github_Credentials {
        private String auth = null;
        private String client_id = null;
        private String client_secret = null;
        public String name = null;
        public String pssw = null;

        public Github_Credentials() {
            String tmp_name = ConfigFactory.load().getString("credentials.indie.github.username");
            String tmp_pssw = ConfigFactory.load().getString("credentials.indie.github.pssw");
            client_id = ConfigFactory.load().getString("credentials.indie.github.client_id");
            client_secret = ConfigFactory.load().getString("credentials.indie.github.client_secret");
            try {
                JsonNode json = play.libs.Json.parse(new FileInputStream("app/controllers/.github_indie_credentials_local_secret"));
                tmp_name = json.get("username").asText();
                tmp_pssw = json.get("pssw").asText();
                client_id = json.get("client_id").asText();
                client_secret = json.get("client_secret").asText();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            name = tmp_name;
            pssw = tmp_pssw;
            Base64.Encoder encoder = Base64.getMimeEncoder();
            String str = tmp_name+":"+tmp_pssw;
            auth = encoder.encodeToString(str.getBytes());
        }
        public String getAuth() {
            return auth;
        }
        public String getClient_id() {
            return client_id;
        }
        public String getClient_secret() {
            return client_secret;
        }
    }

}
