package stores;

import controllers.controller_main;
import models.model_user;

/**
 * Created by skariel on 11/10/15.
 */
public class store_session {
    final static String user_name_session_key = "user_name";
    final static String avatar_url_session_key = "avatar_url";
    final static String state_session_key = "state";
    final static String returnto_session_key = "returnto";
    final static String token_session_key = "token";
    final static String github_code_session_key = "github_code";
    final static String new_repo_session_key = "new_repo___";
    public static final String repo_name_name = "repo_name";
    public static final String repo_homepage_name = "repo_homepage";
    public static final String repo_description_name = "repo_description";

    public static void set_new_repo(controller_main app, String name) {
        String key = new_repo_session_key + name;
        app.session().put(key, "yep!");
    }

    public static boolean pop_new_repo(controller_main app, String name) {
        String key = new_repo_session_key + name;
        boolean ret = app.session().containsKey(key);
        if (ret) {
            app.session().remove(key);
        }
        return ret;
    }

    public static boolean user_is_logged(controller_main app) {
        return get_token(app) != null;
    }

    public static void set_current_user(controller_main app, model_user user) {
        app.session().put(avatar_url_session_key, user.avatar_url);
        app.session().put(user_name_session_key, user.user_name);
    }


    public static String get_avatar_url(controller_main app) {
        if (!user_is_logged(app)) {
            return null;
        }
        return app.session().get(avatar_url_session_key);
    }

    public static String get_user_name(controller_main app) {
        if (!user_is_logged(app)) {
            return null;
        }
        return app.session().get(user_name_session_key);
    }

    public static String get_state(controller_main app) {
        return app.session().get(state_session_key);
    }

    public static void set_state(controller_main app, String state) {
        app.session().put(state_session_key, state);
    }

    public static boolean has_returnto(controller_main app) {
        return app.session().get(returnto_session_key) != null;
    }

    public static void set_return_to(controller_main app, String to) {
        app.session().put(returnto_session_key, to);
    }

    public static String pop_return_to(controller_main app) {
        if (!app.session().containsKey(returnto_session_key)) {
            return null;
        }
        String returnto = app.session().get(returnto_session_key);
        app.session().remove(returnto_session_key);
        return returnto;
    }

    public static void clear(controller_main app) {
        app.session().clear();
    }

    public static void set_token(controller_main app, String token) {
        app.session().put(token_session_key, token);
    }

    public static String get_token(controller_main app) {
        return app.session().get(token_session_key);
    }

    public static void set_github_code(controller_main app, String code) {
        app.session().put(github_code_session_key, code);
    }

    public static String get_github_code(controller_main app) {
        return app.session().get(github_code_session_key);
    }


}
