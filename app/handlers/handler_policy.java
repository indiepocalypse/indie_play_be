package handlers;

import models_db_indie.model_ownership;
import stores.store_conf;
import stores.store_local_db;
import stores.store_session;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by skariel on 24/10/15.
 */
public class handler_policy {

    public static boolean can_create_new_repo() {
        return can_create_new_repo(store_session.get_user_name());
    }

    public static boolean can_create_new_repo(String user_name) {
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_user_name(user_name);
        int total_repos = 0;
        for (model_ownership ownership : ownerships) {
            if (ownership.percent.compareTo(new BigDecimal("50.0")) >= 0) {
                total_repos += 1;
            }

        }
        return total_repos < store_conf.get_policy_maximum_number_of_repos_per_user();
    }
}
