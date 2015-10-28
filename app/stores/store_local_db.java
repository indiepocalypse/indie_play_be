package stores;

import handlers.handler_general;
import models.*;
import models_github.interface_github_webhook;
import models_github.model_webhook_pull_request_created_or_updated;
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
        return (get_repo_by_name(repo_name) != null);
    }

    public static void delete_repo(model_repo repo) {
        delete_offers_by_repo(repo);
        delete_ownerships_by_repo(repo);
        delete_pull_requests_by_repo(repo);
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_repo> repos = model_repo.find
                    .where().eq("repo_name", repo.repo_name).findList();
            if (repos!=null) {
                for (model_repo irepo: repos) {
                    model_pull_request.find.deleteById(irepo.repo_name);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete repo "+repo.repo_name+":\n", e);
        }
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

    public static void update_admin(model_admin admin) {
        try {
            admin.save();
        } catch (Exception ignore) {
            admin.update();
        }
    }

    public static model_admin get_admin_by_name(String name) {
        try {
            return model_admin.find.fetch("user").where().idEq(name+"@admins").findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_admin> get_all_admins() {
        try {
            return model_admin.find.all();
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    public static boolean is_admin(String user_name) {
        return (get_admin_by_name(user_name) != null);
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
            return model_ownership.find.fetch("user").fetch("repo").where().eq("user.user_name", user_name).findList();
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

    public static void delete_ownerships_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_ownership> ownerships = model_ownership.find.fetch("repo")
                    .where().eq("repo.repo_name", repo.repo_name).findList();
            if (ownerships!=null) {
                for (model_ownership ownership: ownerships) {
                    model_ownership.find.deleteById(ownership.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete ownerships by repo:", e);
        }
    }

    /********************************
     * OFFERS!
     ********************************/

    public static void update_offer(model_offer offer) {
        try {
            offer.save();
        } catch (Exception e) {
            offer.update();
        }
    }

    public static List<model_offer> get_offers_by_user(String user_name) {
        try {
            return model_offer.find.fetch("user").fetch("pull_request").fetch("pull_request.repo")
                    .where().eq("user.user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static List<model_offer> get_offers_by_pull_request(String repo_name, int number) {
        try {
            return model_offer.find.fetch("user").fetch("pull_request").fetch("pull_request.repo")
                    .where().eq("pull_request.number", Integer.toString(number))
                    .where().eq("pull_request.repo.repo_name", repo_name)
                    .findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static List<model_offer> get_offers_by_user_by_pull_request(String user_name, String repo_name, int number) {
        try {
            return model_offer.find.fetch("user").fetch("pull_request").fetch("pull_request.repo")
                    .where().eq("user.user_name", user_name)
                    .where().eq("pull_request.number", Integer.toString(number))
                    .where().eq("pull_request.repo.repo_name", repo_name)
                    .findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static void delete_offers_by_pull_request(String repo_name, String number) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_offer> offers = model_offer.find.fetch("user").fetch("pull_request").fetch("pull_request.repo")
                    .where().eq("pull_request.number", number)
                    .where().eq("pull_request.repo.repo_name", repo_name).findList();
            if (offers!=null) {
                for (model_offer offer: offers) {
                    model_offer.find.deleteById(offer.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete offers by pull request:", e);
        }
    }

    public static void delete_offers_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_offer> offers = model_offer.find.fetch("pull_request").fetch("pull_request.repo")
                    .where().eq("pull_request.repo.repo_name", repo.repo_name).findList();
            if (offers!=null) {
                for (model_offer offer: offers) {
                    model_offer.find.deleteById(offer.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete offers by repo:", e);
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

    /********************************
     * PULL REQUESTS!
     ********************************/

    public static boolean update_pull_request(model_pull_request pull_request) {
        // users notification here. Reason is that this is always coupled:
        // when deleting offers, users always need to be notified!
        // return whether update was a real update, in the sense that offers were cleared
        boolean updated = false;
        // check previous pull request, the one we are about to override:
        model_pull_request old_pull_request = get_pull_request_by_repo_name_and_number(pull_request.repo.repo_name, pull_request.number);
        if ((old_pull_request != null) && (!old_pull_request.SHA.equals(pull_request.SHA))) {
            updated = true;
            // updated pull requests contains different code, all previous offers rendered irrelevant
            delete_offers_by_pull_request(pull_request.repo.repo_name, pull_request.number);
            // notify users
            handler_general.notify_by_comment_that_pr_changed_and_offers_are_removed(pull_request);
        }
        try {
            pull_request.save();
        }
        catch (Exception ignored) {
            pull_request.update();
        }
        return updated;
    }

    public static model_pull_request get_pull_request_by_repo_name_and_number(String repo_name, String number) {
        try {
            return model_pull_request.find.fetch("user").fetch("repo")
                    .where().eq("repo.repo_name", repo_name)
                    .where().eq("number", number)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_pull_request> get_pull_requests_by_repo_name(String repo_name) {
        try {
            return model_pull_request.find.fetch("user").fetch("repo")
                    .where().eq("repo.repo_name", repo_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }


    public static List<model_pull_request> get_pull_requests_by_user_name(String user_name) {
        try {
            return model_pull_request.find.fetch("user").fetch("repo")
                    .where().eq("user.user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static void delete_pull_requests_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_pull_request> pull_requests = model_pull_request.find.fetch("repo")
                    .where().eq("repo.repo_name", repo.repo_name).findList();
            if (pull_requests!=null) {
                for (model_pull_request pull_request: pull_requests) {
                    model_pull_request.find.deleteById(pull_request.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete pull_requests by repo:", e);
        }
    }


    /********************************
     * HOOKS!
     ********************************/

    public static void update_hook_components(interface_github_webhook hook) {
        update_user(hook.get_user());
        update_repo(hook.get_repo());
        if (hook.get_pull_request()!=null) {
            // yeah, this is the only field that can be null;
            // (because the hook may be for something else than a pull request!)
            update_pull_request(hook.get_pull_request());
        }
    }
}
