package handlers;

import models.model_ownership;
import models.model_pull_request;
import models.model_repo;
import models.model_user;
import play.Logger;
import stores.store_conf;
import stores.store_github_api;
import stores.store_github_iojs;
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
        // this method assumes repo is not in DB!
        model_user user = get_integrate_github_user_by_name(user_name);
        model_repo repo =  store_github_api.get_repo_by_name(user_name, repo_name);
        store_local_db.update_repo(repo);
        return integrate_github_repo(repo, user, create_webhook);
    }

    public static model_ownership integrate_github_repo(model_repo repo,  model_user user, boolean create_webhook) {
        if (create_webhook) {
            store_github_api.create_webhook(repo);
            create_default_readme_if_not_existing(repo);
        }
        BigDecimal indie_ownership_percent = store_conf.get_default_indie_ownership_percent();
        BigDecimal user_ownership_percent = new BigDecimal("100.0").subtract(indie_ownership_percent);
        model_ownership ownership1 = new model_ownership(user, repo, user_ownership_percent);
        model_user theindiepocalypse = store_local_db.get_user_by_name("theindiepocalypse");
        model_ownership ownership2 = new model_ownership(theindiepocalypse, repo, indie_ownership_percent);
        store_local_db.update_ownership(ownership1);
        store_local_db.update_ownership(ownership2);
        return ownership1;
    }

    public static void create_default_readme_if_not_existing(model_repo repo) {
        if (store_github_api.has_readme(repo.repo_name)) {
            Logger.info("repo "+repo.repo_name+" already has a readme. Skipping creation of default one");
            return;
        }
        Logger.info("Creating a default readme for repo "+repo.repo_name);
        String content = "This is the default readme. It's needed so the repo can be forked";
        if (store_github_iojs.create_readme(repo, content)) {
            Logger.info("    successfuly created the default readme for repo "+repo.repo_name);
        }
        else {
            Logger.error("    Problem createing the default readme for repo " + repo.repo_name);
        }
    }

    public static void handle_updated_pull_request(model_pull_request pull_request) {
        store_github_api.comment_on_issue(pull_request.repo, pull_request.number,
                "PR updated, all offers cleared!\nplease plase your new offers");
    }
}
