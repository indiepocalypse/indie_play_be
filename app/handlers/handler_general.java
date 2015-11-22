package handlers;

import controllers.routes;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import play.Logger;
import stores.*;
import sync.sync_gmail;

import java.math.BigDecimal;

/**
 * Created by skariel on 15/10/15.
 */
public class handler_general {

    public static model_user get_integrate_github_user_by_name(String name) {
        // search user in database...
        model_user user = store_local_db.get_user_by_name(name);
        if (user == null) {
            // not found, update from github
            try {
                user = store_github_api.get_user_by_name(name);
                store_local_db.update_user(user);
            }
            catch (github_io_exception ignore) {
            }
        } else {
            Logger.info("user " + name + " already in DB, will not integrate user");
        }
        return user;
    }

    public static void integrate_github_repo(String repo_name, String user_name, boolean create_webhook,
                                                        boolean check_for_existance_of_readme,
                                                        boolean delete_original_collaborators) throws github_io_exception {
        // this method assumes repo is not in DB!
        model_user user = get_integrate_github_user_by_name(user_name);
        model_repo repo = store_github_api.get_repo_by_name(user_name, repo_name);
        integrate_github_repo(repo, user, create_webhook, check_for_existance_of_readme, delete_original_collaborators);
    }

    public static void integrate_github_repo(model_repo repo, model_user user, boolean create_webhook,
                                                        boolean check_for_existance_first,
                                                        boolean delete_original_collaborators) throws github_io_exception {
        store_local_db.update_repo(repo);
        if (create_webhook) {
            store_github_api.create_webhook(repo);
        }
        BigDecimal indie_ownership_percent = store_conf.get_default_indie_ownership_percent();
        BigDecimal user_ownership_percent = new BigDecimal("100.0").subtract(indie_ownership_percent);
        final boolean is_creator = true;
        model_ownership ownership1 = new model_ownership(user, repo, user_ownership_percent, is_creator);
        model_user theindiepocalypse = store_local_db.get_user_by_name("theindiepocalypse");
        final boolean indiepocalypse_is_creator = false;
        model_ownership ownership2 = new model_ownership(theindiepocalypse, repo, indie_ownership_percent, indiepocalypse_is_creator);
        store_local_db.update_ownership(ownership1);
        store_local_db.update_ownership(ownership2);
        // TODO: should return the policy too?
        model_repo_policy policy = new model_repo_policy(repo);
        store_local_db.update_policy(policy);

        create_default_readme(repo, check_for_existance_first);

        if (delete_original_collaborators) {
            try {
                store_github_api.delete_all_collaborators_from_repo(ownership1.repo);
                Logger.info("user " + user.user_name + " removed from collaborators to " + ownership1.repo.repo_name);
                final String user_mail = store_github_api.get_user_mail(user.user_name);
                final String mail_subject = "You were removed as collaborator from repository (" + repo.repo_name + ")";
                final String mail_body = "The reason is that this repo was transferred to thindipocalypse user and it is now managed through its api.\n see the FAQ here:\n" + store_conf.get_absolute_url(routes.controller_main.faq().url());
                sync_gmail.sendmail(user_mail, mail_subject, mail_body);
            }
            catch (github_io_exception e) {
                Logger.error("could not remove user " + user.user_name + " removed from collaborators to " + ownership1.repo.repo_name);
            }

        }
    }

    public static void create_default_readme(model_repo repo, boolean check_for_existance_first) {
        if (check_for_existance_first) {
            try {
                store_github_api.has_readme(repo.repo_name);
            }
            catch (github_io_exception e) {
                Logger.info("repo " + repo.repo_name + " already has a readme. Skipping creation of default one");
                return;
            }
        }
        Logger.info("Creating a default readme for repo " + repo.repo_name);
        String content = "This is the default readme. It's needed so the repo can be forked";
        if (store_github_iojs.create_readme(repo, content)) {
            Logger.info("    successfuly created the default readme for repo " + repo.repo_name);
        } else {
            Logger.error("    Problem createing the default readme for repo " + repo.repo_name);
        }
    }

    public static void notify_by_comment_that_pr_changed_and_offers_are_removed(model_pull_request pull_request) throws github_io_exception {
        store_github_api.comment_on_issue(pull_request.repo, pull_request.number,
                "PR updated, all offers cleared!\nplease place your new offers");
    }

    private static void __delete_repo(model_repo repo) {
        try {
            model_repo.find.deleteById(repo.repo_name);
        } catch (Exception e) {
            Logger.error("failed to delete repo " + repo.repo_name + ":\n", e);
        }
    }


    public static void delete_repo_from_github_and_db_and_also_related_ownership_policy_offers(model_repo repo) throws github_io_exception {
        store_local_db.delete_requests_by_repo(repo);
        store_local_db.delete_offers_by_repo(repo);
        store_local_db.delete_ownerships_by_repo(repo);
        store_local_db.delete_policy_by_repo(repo);
        store_local_db.delete_pull_requests_by_repo(repo);
        __delete_repo(repo);
        store_github_api.delete_repo(repo);
    }

    public static boolean update_pull_request_and_clear_offers_if_necessary(model_pull_request pull_request) {
        // this method deletes offers is pull reuqest was updated.
        // users notification here. Reason is that this is always coupled:
        // when deleting offers, users always need to be notified!
        // return whether update was a real update, in the sense that offers were cleared
        boolean updated = false;
        // check previous pull request, the one we are about to override:
        model_pull_request old_pull_request = store_local_db.get_pull_request_by_repo_name_and_number(pull_request.repo.repo_name, pull_request.number);
        if ((old_pull_request != null) && (!old_pull_request.SHA.equals(pull_request.SHA))) {
            updated = true;
            // updated pull requests contains different code, all previous offers rendered irrelevant
            store_local_db.delete_request_by_pull_request(pull_request.repo.repo_name, pull_request.number);
            store_local_db.delete_offers_by_pull_request(pull_request.repo.repo_name, pull_request.number);
            // notify users
            try {
                notify_by_comment_that_pr_changed_and_offers_are_removed(pull_request);
            }
            catch (github_io_exception ignore) {
            }
        }
        try {
            pull_request.save();
        } catch (Exception ignored) {
            pull_request.update();
        }
        return updated;
    }
}
