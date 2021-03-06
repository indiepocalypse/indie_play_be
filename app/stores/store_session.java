package stores;

import controllers.controller_main;
import models_db_github.model_user;

import javax.annotation.Nonnull;

/**
 * Created by skariel on 11/10/15.
 */
public class store_session {
    public static final String mail_name_name = "mail_name";
    public static final String contact_message_description_name = "contact_message_description_name";
    public static final String repo_name_name = "repo_name";
    public static final String repo_homepage_name = "repo_homepage";
    public static final String repo_description_name = "repo_description";
    private final static String user_name_session_key = "user_name";
    private final static String user_is_admin_session_key = "user_is_admin";
    private final static String avatar_url_session_key = "avatar_url";
    private final static String state_session_key = "state";
    private final static String returnto_session_key = "returnto";
    private final static String token_session_key = "token";
    private final static String github_code_session_key = "github_code";
    private final static String new_repo_session_key = "new_repo___";
    private final static String user_github_html_url_key = "user_github_html_url_key___";

    public static String get_path() {
        return controller_main.request().path();
    }

    public static void set_new_repo(String name) {
        String key = new_repo_session_key + name;
        controller_main.session().put(key, "yep!");
    }

    public static boolean pop_new_repo(String name) {
        String key = new_repo_session_key + name;
        boolean ret = controller_main.session().containsKey(key);
        if (ret) {
            controller_main.session().remove(key);
        }
        return ret;
    }

    public static boolean user_is_logged() {
        return get_token() != null;
    }

    public static void set_current_user(model_user user) {
        controller_main.session().put(avatar_url_session_key, user.avatar_url);
        controller_main.session().put(user_name_session_key, user.user_name);
        controller_main.session().put(user_github_html_url_key, user.github_html_url);
    }


    public static String get_avatar_url() {
        assert user_is_logged();
        @Nonnull final String avatar_url = controller_main.session().get(avatar_url_session_key);
        assert avatar_url != null;
        return avatar_url;
    }

    public static String get_user_github_html_url() {
        assert user_is_logged();
        @Nonnull final String user_github_html_url = controller_main.session().get(user_github_html_url_key);
        assert user_github_html_url != null;
        return user_github_html_url;
    }

    public static @Nonnull String get_user_name() {
        assert user_is_logged();
        @Nonnull final String user_name = controller_main.session().get(user_name_session_key);
        assert user_name != null;
        return user_name;
    }

    public static String get_state() {
        return controller_main.session().get(state_session_key);
    }

    public static void set_state(String state) {
        controller_main.session().put(state_session_key, state);
    }

    public static boolean has_returnto() {
        return controller_main.session().get(returnto_session_key) != null;
    }

    public static void set_return_to(String to) {
        controller_main.session().put(returnto_session_key, to);
    }

    public static String pop_return_to() {
        if (!controller_main.session().containsKey(returnto_session_key)) {
            return null;
        }
        String returnto = controller_main.session().get(returnto_session_key);
        controller_main.session().remove(returnto_session_key);
        return returnto;
    }

    public static void clear() {
        controller_main.session().clear();
    }

    private static String get_token() {
        return controller_main.session().get(token_session_key);
    }

    public static void set_token(String token) {
        controller_main.session().put(token_session_key, token);
    }

    public static String get_github_code() {
        return controller_main.session().get(github_code_session_key);
    }

    public static void set_github_code(String code) {
        controller_main.session().put(github_code_session_key, code);
    }

    public static void set_admin(Boolean admin) {
        controller_main.session().put(user_is_admin_session_key, admin.toString());
    }

    public static boolean user_is_admin() {
        return (get_token() != null) && (controller_main.session().containsKey(user_is_admin_session_key)) &&
                (Boolean.parseBoolean(controller_main.session().get(user_is_admin_session_key)));
    }

    public model_user get_user() {
        return new model_user(get_user_name(), get_user_github_html_url(), get_avatar_url());
    }
}
