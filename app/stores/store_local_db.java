package stores;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import controllers.controller_main;
import models.model_ownership;
import models.model_repo;
import models.model_user;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 05/10/15.
 */


// store for inner db and session stuff (ie doesn't call to github, gmail or whatever
public class store_local_db {
    // TODO: split a store_session
    // TODO: split a credentials store

    public static final String repo_name_name = "repo_name";
    public static final String repo_homepage_name = "repo_homepage";
    public static final String repo_description_name = "repo_description";
    final static String user_name_session_key = "user_name";
    final static String avatar_url_session_key = "avatar_url";
    final static String state_session_key = "state";
    final static String returnto_session_key = "returnto";
    final static String token_session_key = "token";
    final static String github_code_session_key = "github_code";
    final static String new_repo_session_key = "new_repo___";
    final static Github_Credentials github_credentials = new Github_Credentials();

    public static WSClient getwsclient() {
        WSClient ws;
        try {
            ws = WS.client();
        } catch (Exception ignored) {
            ws = WS.newClient(1);
        }
        return ws;
    }

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

    /*************************************************************
     * Github stuff!
     ************************************************************/

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

    /********************************
     * REPOS!
     ********************************/

    // this updates a repo in the db according to the repo given in the parameters
    public static void update_repo(model_repo repo) {
        try {
            repo.save();
        } catch (Exception ignore) {
            repo.update();
        }
    }

    public static model_repo get_repo_by_name(String repo_name) {
        try {
            return model_repo.find.byId(repo_name);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_repo> get_all_repos() {
        try {
            return model_repo.find.all();
        } catch (Exception ignore) {
            return new ArrayList<model_repo>();
        }
    }

    public static void register_transfered_repo(model_user user, model_repo repo) {
        update_repo(repo);
        update_user(user);
        model_ownership ownership = new model_ownership(user, repo, new BigDecimal("100.0"));
        store_local_db.update_ownership(ownership);
    }

    public static void register_new_repo(controller_main app, model_repo repo) {
        set_new_repo(app, repo.repo_name);
        model_user user = store_github_api.get_user_by_name(get_user_name(app));
        register_transfered_repo(user, repo);
    }

    /********************************
     * USERS!
     ********************************/

    public static void update_user(model_user user) {
        try {
            user.save();
        } catch (Exception ignore) {
            user.update();
        }
    }

    public static model_user get_user_by_name(String user_name) {
        try {
            return model_user.find.byId(user_name);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_user> get_all_users() {
        try {
            return model_user.find.all();
        } catch (Exception ignore) {
            return new ArrayList<model_user>();
        }
    }

    /********************************
     * OWNERSHIP!
     ********************************/

    public static void update_ownership(model_ownership ownership) {
        try {
            Logger.info("============ownership id  : " + ownership.id);
            Logger.info("============repo_name     : " + ownership.repo.repo_name);
            Logger.info("============user_name     : " + ownership.user.user_name);
            ownership.save();
        } catch (Exception e) {
            Logger.error("=-=-=-=-=-=-=-=-=-=" + e.toString());
            ownership.update();
        }
    }

    public static List<model_ownership> get_ownerships_by_user_name(String user_name) {
        try {
            return model_ownership.find.where().eq("user.user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static List<model_ownership> get_ownerships_by_repo_name(String repo_name) {
        try {
            return model_ownership.find.where().eq("repo.repo_name", repo_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    /********************************
     * SYNC STUFF!
     ********************************/

    public static long get_github_repo_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.github.repo.delta_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_gmail_reload_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.gmail.reload.delta_milis", TimeUnit.MILLISECONDS);
    }

    static class Github_Credentials {
        public String name = null;
        public String pssw = null;
        private String auth = null;
        private String client_id = null;
        private String client_secret = null;

        public Github_Credentials() {
            String tmp_name = ConfigFactory.load().getString("credentials.indie.github.username");
            String tmp_pssw = ConfigFactory.load().getString("credentials.indie.github.pssw");
            client_id = ConfigFactory.load().getString("credentials.indie.github.client_id");
            client_secret = ConfigFactory.load().getString("credentials.indie.github.client_secret");
            try {
                JsonNode json = play.libs.Json.parse(new FileInputStream("app/stores/.github_indie_credentials_local_secret"));
                tmp_name = json.get("username").asText();
                tmp_pssw = json.get("pssw").asText();
                client_id = json.get("client_id").asText();
                client_secret = json.get("client_secret").asText();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            name = tmp_name;
            pssw = tmp_pssw;
            Base64.Encoder encoder = Base64.getMimeEncoder();
            String str = tmp_name + ":" + tmp_pssw;
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
