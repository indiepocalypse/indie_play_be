package controllers;

import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

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
}
