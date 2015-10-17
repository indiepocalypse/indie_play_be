package handlers;

import models.model_ownership;
import models.model_repo;
import models.model_user;
import play.Logger;
import stores.store_github_api;
import stores.store_local_db;

import java.math.BigDecimal;

/**
 * Created by skariel on 15/10/15.
 */
public class handler_general {

    public static model_user get_integrate_github_user_by_name(String name) {
        // search user in database...
        model_user user = store_local_db.get_user_by_name(name);
        if (user==null) {
            // not found, update from github
            user = store_github_api.get_user_by_name(name);
            store_local_db.update_user(user);
        }
        else {
            Logger.info("user " + name + " already in DB, will not integrate user");
        }
        return user;
    }

    public static model_ownership integrate_github_repo(String repo_name, String user_name, boolean create_webhook) {
        model_user user = get_integrate_github_user_by_name(user_name);
        // search repo in db
        model_repo repo = store_local_db.get_repo_by_name(repo_name);
        if (repo != null) {
            Logger.info("repo \"" + repo_name + "\" already in DB. Will not integrate...");
            return null;
        }
        // not found, update from github
        repo = store_github_api.get_repo_by_name(user_name, repo_name);
        store_local_db.update_repo(repo);
        return integrate_github_repo(repo, user, create_webhook);
    }

    public static model_ownership integrate_github_repo(model_repo repo,  model_user user, boolean create_webhook) {
        if (create_webhook) {
            store_github_api.create_webhook(repo);
        }
        model_ownership ownership = new model_ownership(user, repo, new BigDecimal("100.0"));
        store_local_db.update_ownership(ownership);
        return ownership;
    }


}
