package stores;

import models.model_gmail_last_date_read;
import models.model_ownership;
import models.model_repo;
import models.model_user;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skariel on 05/10/15.
 */


// store for inner db and session stuff (ie doesn't call to github, gmail or whatever
public class store_local_db {

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
            return new ArrayList<>();
        }
    }

    public static boolean has_repo(String repo_name) {
        // TODO: is this the best way to check?!
        return (get_repo_by_name(repo_name) != null);
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
            return new ArrayList<>();
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
            return model_ownership.find.fetch("user").fetch("repo").where().eq("repo.repo_name", repo_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }


    /********************************
     * GMAIL!
     ********************************/

    public static void update_gmail_last_read_date(model_gmail_last_date_read gmail_sync_date) {
        try {
            gmail_sync_date.save();
        } catch (Exception e) {
            gmail_sync_date.update();
            Logger.info("gamail sync date updated...");
        }
    }

    public static model_gmail_last_date_read get_gmail_latest_sync_date() {
        return model_gmail_last_date_read.get_a_copy_of_the_singleton();
    }

}
