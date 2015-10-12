package stores;

import controllers.controller_main;
import models.model_ownership;
import models.model_repo;
import models.model_user;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by skariel on 05/10/15.
 */


// store for inner db and session stuff (ie doesn't call to github, gmail or whatever
public class store_local_db {
    // TODO: split a store_session
    // TODO: split a credentials store

    public static WSClient getwsclient() {
        WSClient ws;
        try {
            ws = WS.client();
        } catch (Exception ignored) {
            ws = WS.newClient(1);
        }
        return ws;
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
        update_ownership(ownership);
    }

    public static void register_new_repo(model_repo repo) {
        store_session.set_new_repo(repo.repo_name);
        model_user user = store_github_api.get_user_by_name(store_session.get_user_name());
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
            ownership.save();
        } catch (Exception e) {
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
}
