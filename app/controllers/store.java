package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import models.ownership_model;
import models.repo_model;
import models.user_model;
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
public class store {
    // TODO: refactor this class into its own package!

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

    public static void set_new_repo(ApplicationRoutes app, String name) {
        String key = new_repo_session_key + name;
        app.session().put(key, "yep!");
    }

    public static boolean pop_new_repo(ApplicationRoutes app, String name) {
        String key = new_repo_session_key + name;
        boolean ret = app.session().containsKey(key);
        if (ret) {
            app.session().remove(key);
        }
        return ret;
    }

    public static boolean user_is_logged(ApplicationRoutes app) {
        return get_token(app) != null;
    }

    public static void set_current_user(ApplicationRoutes app, user_model user) {
        app.session().put(avatar_url_session_key, user.avatar_url);
        app.session().put(user_name_session_key, user.user_name);
    }


    public static String get_avatar_url(ApplicationRoutes app) {
        if (!user_is_logged(app)) {
            return null;
        }
        return app.session().get(avatar_url_session_key);
    }

    public static String get_user_name(ApplicationRoutes app) {
        if (!user_is_logged(app)) {
            return null;
        }
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

    public static void clear(ApplicationRoutes app) {
        app.session().clear();
    }

    /*************************************************************
     * Github stuff!
     ************************************************************/

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
    public static void update_repo(repo_model repo) {
        try {
            repo.save();
        } catch (Exception ignore) {
            repo.update();
        }
    }

    public static repo_model get_repo_by_name(String repo_name) {
        try {
            return repo_model.find.byId(repo_name);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<repo_model> get_all_repos() {
        try {
            return repo_model.find.all();
        } catch (Exception ignore) {
            return new ArrayList<repo_model>();
        }
    }

    public static void register_transfered_repo(user_model user, repo_model repo) {
        update_repo(repo);
        update_user(user);
        ownership_model ownership = new ownership_model(user, repo, new BigDecimal("100.0"));
        store.update_ownership(ownership);
    }

    public static void register_new_repo(ApplicationRoutes app, repo_model repo) {
        set_new_repo(app, repo.repo_name);
        user_model user = github_access.get_user_by_name(get_user_name(app));
        register_transfered_repo(user, repo);
    }

    /********************************
     * USERS!
     ********************************/

    public static void update_user(user_model user) {
        try {
            user.save();
        } catch (Exception ignore) {
            user.update();
        }
    }

    public static user_model get_user_by_name(String user_name) {
        try {
            return user_model.find.byId(user_name);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<user_model> get_all_users() {
        try {
            return user_model.find.all();
        } catch (Exception ignore) {
            return new ArrayList<user_model>();
        }
    }

    /********************************
     * OWNERSHIP!
     ********************************/

    public static void update_ownership(ownership_model ownership) {
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

    public static List<ownership_model> get_ownerships_by_user_name(String user_name) {
        try {
            return ownership_model.find.where().eq("user.user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static List<ownership_model> get_ownerships_by_repo_name(String repo_name) {
        try {
            return ownership_model.find.where().eq("repo.repo_name", repo_name).findList();
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
                JsonNode json = play.libs.Json.parse(new FileInputStream("app/controllers/.github_indie_credentials_local_secret"));
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
