package stores;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlRow;
import handlers.handler_general;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_db_indie.*;
import models_memory_github.interface_github_webhook;
import play.Logger;

import java.util.*;

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
        } catch (Exception e1) {
            try {
                repo.update();
            } catch (Exception e) {
                Logger.error("cannot save repo ", e1);
                Logger.error("could not update model_repo", e);
                throw e;
            }
        }
    }

    public static model_repo get_repo_by_name(String repo_name) {
        try {
            return model_repo.fetch().where().idEq(repo_name).findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_repo> get_all_repos() {
        try {
            return model_repo.fetch().where().findList();
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    public static boolean has_repo(String repo_name) {
        return (get_repo_by_name(repo_name) != null);
    }

    /********************************
     * USERS!
     ********************************/

    public static void update_user(model_user user) {
        try {
            user.save();
        } catch (Exception e1) {
            try {
                user.update();
            } catch (Exception e) {
                Logger.error("cannot save user ", e1);
                Logger.error("could not update model_user", e);
                throw e;
            }
        }
    }

    public static model_user get_user_by_name(String user_name) {
        try {
            return model_user.fetch().where().idEq(user_name).findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_user> get_all_users() {
        try {
            return model_user.fetch().where().findList();
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    /********************************
     * MERGE TRANSACTIONS!
     ********************************/

    public static void update_merge_transaction(model_merge_transaction merge_transaction) {
        try {
            merge_transaction.save();
        } catch (Exception e1) {
            try {
                merge_transaction.update();
            } catch (Exception e) {
                Logger.error("cannot save merge_transaction ", e1);
                Logger.error("could not update merge transaction", e);
                throw e;
            }
        }
    }

    public static List<model_merge_transaction> get_merge_transactions_by_to_user(model_user to_user) {
        try {
            return model_merge_transaction.fetch().where()
                    .eq("to_user.user_name", to_user.user_name)
                    .findList();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_merge_transaction> get_merge_transactions_by_from_user(model_user from_user) {
        try {
            return model_merge_transaction.fetch().where()
                    .eq("from_user.user_name", from_user.user_name)
                    .findList();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_merge_transaction> get_merge_transactions_by_pull_request(model_pull_request pull_request) {
        try {
            // TODO: refactor all these fetches into the model itself
            return model_merge_transaction.fetch().where()
                    .eq("pull_request.SHA", pull_request.SHA)
                    .findList();
        } catch (Exception ignore) {
            return null;
        }
    }


    /********************************
     * OWNERSHIP!
     ********************************/

    public static void update_ownership(model_ownership ownership) {
        try {
            ownership.save();
        } catch (Exception e1) {
            try {
                ownership.update();
            } catch (Exception e) {
                Logger.error("cannot save ownership ", e1);
                Logger.error("could not update ownership", e);
                throw e;
            }
        }
    }

    public static List<model_ownership> get_ownerships_by_user_name(String user_name) {
        try {
            return model_ownership.fetch().where().eq("user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static model_ownership get_creator_by_repo(model_repo repo) {
        try {
            return model_ownership.fetch()
                    .where().eq("repo_name", repo.repo_name)
                    .where().eq("is_creator", true)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_ownership> get_ownerships_by_repo_name(String repo_name) {
        try {
            return model_ownership.fetch().where().eq("repo_name", repo_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static Map<model_user, model_ownership> get_users_and_ownerships_by_repo_name(String repo_name) {
        try {
            final String c1 = model_ownership.class.getName();
            final String c2 = model_user.class.getName();

            final String sql = "select * from "+c1+" inner join "+c2+" on "+c1+".user_name="+c2+".user_name";
            Logger.info("XXXXXXXXXXXX =========> "+sql);
            Map<model_user, model_ownership> map = new HashMap<>(3);

            Logger.info("XXXXXXXXXXXXX ========> before");
            List<SqlRow> rows = Ebean.createSqlQuery(sql).findList();
            Logger.info("XXXXXXXXXXXXX ========> after");


            for (SqlRow row: rows) {
                map.put(model_user.from_sqlrow(row), model_ownership.from_sqlrow(row));
            }
            Logger.info("XXXXXXXXXXXX =========> "+map.size());
            return map;
        } catch (Exception ignore) {
            return new HashMap<>(0);
        }
    }

    public static model_ownership get_ownership_by_user_name_and_repo_name(model_user user, model_repo repo) {
        try {
            return model_ownership.fetch().where()
                    .eq("repo_name", repo.repo_name)
                    .eq("user_name", user.user_name)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static model_ownership get_ownership_by_id(String ownership_id) {
        try {
            return model_ownership.fetch().where().idEq(ownership_id)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static void delete_ownerships_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_ownership> ownerships = model_ownership.fetch()
                    .where().eq("repo_name", repo.repo_name).findList();
            if (ownerships != null) {
                for (model_ownership ownership : ownerships) {
                    model_ownership.deleteById(ownership.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete ownerships by repo:", e);
        }
    }

    /********************************
     * OFFERS FOR PRS!
     ********************************/

    public static void update_offer(model_offer_for_merge offer) {
        try {
            offer.save();
        } catch (Exception e1) {
            try {
                offer.update();
            } catch (Exception e) {
                Logger.error("cannot save offer ", e1);
                Logger.error("could not update offer", e);
                throw e;
            }
        }
    }

    public static List<model_offer_for_merge> get_offers_by_user(String user_name) {
        try {
            return model_offer_for_merge.fetch()
                    .where().eq("user.user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static List<model_offer_for_merge> get_offers_by_pull_request(String repo_name, String number) {
        try {
            return model_offer_for_merge.fetch()
                    .where().eq("pull_request.number", number)
                    .where().eq("pull_request.repo.repo_name", repo_name)
                    .findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static model_offer_for_merge get_offer_by_user_by_pull_request(String user_name, String repo_name, String number) {
        try {
            return model_offer_for_merge.fetch()
                    .where().eq("user.user_name", user_name)
                    .where().eq("pull_request.number", number)
                    .where().eq("pull_request.repo.repo_name", repo_name)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static model_offer_for_merge get_offer_by_id(String offer_id) {
        try {
            return model_offer_for_merge.fetch().where().idEq(offer_id)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static void delete_offers_by_pull_request(String repo_name, String number) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_offer_for_merge> offers = model_offer_for_merge.fetch()
                    .where().eq("pull_request.number", number)
                    .where().eq("pull_request.repo.repo_name", repo_name).findList();
            if (offers != null) {
                for (model_offer_for_merge offer : offers) {
                    model_offer_for_merge.deleteById(offer.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete offers by pull request:", e);
        }
    }

    public static void delete_offers_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_offer_for_merge> offers = model_offer_for_merge.fetch()
                    .where().eq("pull_request.repo.repo_name", repo.repo_name).findList();
            if (offers != null) {
                for (model_offer_for_merge offer : offers) {
                    model_offer_for_merge.deleteById(offer.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete offers by repo:", e);
        }
    }

    /********************************
     * REQUESTS FOR PRs!
     ********************************/

    public static void update_request(model_request_for_merge request) {
        try {
            request.save();
        } catch (Exception e1) {
            try {
                request.update();
            } catch (Exception e) {
                Logger.error("cannot save request ", e1);
                Logger.error("could not update model_request_for_merge", e);
                throw e;
            }
        }
    }

    public static List<model_request_for_merge> get_requests_by_user(String user_name) {
        try {
            return model_request_for_merge.fetch()
                    .where().eq("user.user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static model_request_for_merge get_request_by_pull_request(String repo_name, String number) {
        try {
            return model_request_for_merge.fetch()
                    .where().eq("pull_request.number", number)
                    .where().eq("pull_request.repo.repo_name", repo_name)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static void delete_request_by_pull_request(String repo_name, String number) {
        try {
            model_request_for_merge.fetch()
                    .where().eq("pull_request.number", number)
                    .where().eq("pull_request.repo.repo_name", repo_name)
                    .findUnique().delete();
        } catch (Exception e) {
            Logger.error("failed to delete request by pull request:", e);
        }
    }

    public static void delete_requests_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_request_for_merge> requests = model_request_for_merge.fetch()
                    .where()
                    .eq("pull_request.repo.repo_name", repo.repo_name)
                    .findList();
            if (requests != null) {
                for (model_request_for_merge offer : requests) {
                    model_request_for_merge.deleteById(offer.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete requests by repo:", e);
        }
    }

    /********************************
     * POLICIES!
     ********************************/

    public static void update_policy(model_repo_policy policy) {
        try {
            policy.save();
        } catch (Exception e1) {
            try {
                policy.update();
            } catch (Exception e) {
                Logger.error("cannot save repo_policy ", e1);
                Logger.error("could not update model_repo_policy", e);
                throw e;
            }
        }
    }

    public static model_repo_policy get_policy_by_repo(model_repo repo) {
        try {
            return model_repo_policy.fetch()
                    .where().eq("repo.repo_name", repo.repo_name).findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static void delete_policy_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_repo_policy> policies = model_repo_policy.fetch()
                    .where().eq("repo.repo_name", repo.repo_name).findList();
            if (policies != null) {
                for (model_repo_policy policy : policies) {
                    model_repo_policy.deleteById(policy.id);
                }
            }
        } catch (Exception e) {
            Logger.error("failed to delete policies by repo:", e);
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

    public static model_pull_request get_pull_request_by_repo_name_and_number(String repo_name, String number) {
        try {
            return model_pull_request.fetch()
                    .where().eq("repo_name", repo_name)
                    .where().eq("number", number)
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<model_pull_request> get_pull_requests_by_repo_name(String repo_name) {
        try {
            return model_pull_request.fetch()
                    .where().eq("repo_name", repo_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }


    public static List<model_pull_request> get_pull_requests_by_user_name(String user_name) {
        try {
            return model_pull_request.fetch()
                    .where().eq("user_name", user_name).findList();
        } catch (Exception ignore) {
            return new ArrayList<>(0);
        }
    }

    public static void delete_pull_requests_by_repo(model_repo repo) {
        try {
            // TODO: this delete one by one is bad. Fix it!
            List<model_pull_request> pull_requests = model_pull_request.fetch()
                    .where().eq("repo_name", repo.repo_name).findList();
            if (pull_requests != null) {
                for (model_pull_request pull_request : pull_requests) {
                    model_pull_request.deleteById(pull_request.id);
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
        if (hook.get_pull_request() != null) {
            // yeah, this is the only field that can be null;
            // (because the hook may be for something else than a pull request!)
            handler_general.locally_update_pull_request_and_clear_offers_if_necessary(hook.get_pull_request());
        }
    }

    /********************************
     * IMAGES!
     ********************************/

    public static void update_repo_image(model_repo_image model_repo_image) {
        try {
            model_repo_image.save();
        } catch (Exception e1) {
            try {
                model_repo_image.update();
            } catch (Exception e) {
                Logger.error("cannot save repo_image ", e1);
                Logger.error("could not update model_repo_image", e);
                throw e;
            }
        }
    }

    public static model_repo_image get_repo_image_by_file_name(String file_name) {
        try {
            return model_repo_image.fetch()
                    .where().idEq(file_name)
                    .findUnique();
        } catch (Exception e) {
            Logger.error("WHILE FETCHING IMAGE WITH NAME "+file_name+": ", e);
            return null;
        }
    }

    public static List<model_repo_image> get_all_repo_images(String repo_name) {
        try {
            return model_repo_image.fetch()
                    .where().eq("repo.repo_name", repo_name)
                    .findList();
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<Object> get_all_repo_images_id(String repo_name) {
        try {
            return model_repo_image.fetch()
                    .where().eq("repo.repo_name", repo_name)
                    .findIds();
        } catch (Exception ignore) {
            return null;
        }
    }

    /********************************
     * USER INTERACTIONS!
     ********************************/

    public static void update_user_interaction(model_user_interaction model_user_interaction) {
        try {
            model_user_interaction.save();
        } catch (Exception e1) {
            try {
                model_user_interaction.update();
            } catch (Exception e) {
                Logger.error("cannot save model_user_interaction ", e1);
                Logger.error("could not update model_user_interaction", e);
                throw e;
            }
        }
    }

    public static List<model_user_interaction> get_user_interactions(String user_name) {
        try {
            return model_user_interaction.fetch()
                    .where()
                    .eq("user_name", user_name)
                    .orderBy("date_performed")
                    .findList();
        } catch (Exception e) {
            Logger.error("WHILE FETCHING USER INTERACTIONS FOR "+user_name+": ", e);
            return null;
        }
    }

    public static PagedList<model_user_interaction> get_user_interactions_paginated(String user_name, int per_page, int page_num) {
        try {
            PagedList<model_user_interaction> result = model_user_interaction.fetch()
                    .where()
                    .eq("user_name", user_name)
                    .orderBy("date_performed")
                    .findPagedList(page_num, per_page);
            return result;
        } catch (Exception e) {
            Logger.error("WHILE FETCHING USER INTERACTIONS FOR "+user_name+": ", e);
            return null;
        }
    }

    public static int get_user_interactions_count_during_last_milis(String user_name, long milis) {
        Date past_date = new Date();
        past_date.setTime(past_date.getTime()-milis);
        try {
            return model_user_interaction.fetch()
                    .where()
                    .eq("user_name", user_name)
                    .ge("date_performed", past_date)
                    .findRowCount();
        }
        catch (Exception e) {
            Logger.error("WHILE COUNTING USER INTERACTIONS FOR "+user_name+": ", e);
            return -1;
        }
    }

    /********************************
     * USER_EXTENDED_INFO
     ********************************/

    public static void update_user_extended_info(model_user_extended_info user_extended_info) {
        try {
            user_extended_info.save();
        } catch (Exception e1) {
            try {
                user_extended_info.update();
            } catch (Exception e) {
                Logger.error("cannot save user_extended_info ", e1);
                Logger.error("could not update model_user_extended_info", e);
                throw e;
            }
        }
    }

    public static model_user_extended_info get_user_extended_info(String user_name) {
        try {
            return model_user_extended_info.fetch()
                    .where()
                    .idEq(model_user_extended_info.get_id_by_user_name(user_name))
                    .findUnique();
        } catch (Exception ignore) {
            return null;
        }
    }
    public static List<model_user_extended_info> get_all_admins() {
        try {
            return model_user_extended_info.fetch().
                    where().eq("is_admin", true)
                    .findList();
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    public static boolean is_admin(String user_name) {
        model_user_extended_info user_extended_info = get_user_extended_info(user_name);
        return ((user_extended_info==null) || (user_extended_info.is_admin));
    }

}
