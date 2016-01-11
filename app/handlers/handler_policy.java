package handlers;

import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import play.Logger;
import stores.store_conf;
import stores.store_local_db;
import stores.store_session;

import javax.annotation.Nonnull;
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

    public static boolean is_rate_limited(String user_name) {
        if (user_name == null) {
            // not logged in user will always be rate limited
            Logger.info("Rate limit checked, user_name==null, so ratelimiting!");
            return true;
        }
        int number_of_actions_in_relevant_timeframe = store_local_db.get_user_interactions_count_during_last_milis(user_name, store_conf.get_delay_L2_milis());
        if (number_of_actions_in_relevant_timeframe < 0) {
            // could not find number of actions, user should be rate limited
            Logger.info("Rate limit checked for user "+user_name+" could not retrieve number of actions in timeframe, hence ratelimiting");
            return true;
        }
        Logger.info("rate limit for user " + user_name + " checked, current value is " + Integer.toString(number_of_actions_in_relevant_timeframe) + " allowed " + Integer.toString(store_conf.get_rate_limit_for_L2_delay()));
        return number_of_actions_in_relevant_timeframe > store_conf.get_rate_limit_for_L2_delay();
    }

    public enum enum_upload_image_policy_result {
        NULL_NAME,
        NULL_OWNERHSIP,
        NOT_ENOUGH_OWNERSHIP,
        NO_REPO_POLICY,
        SEE_CAN_UPLOAD_FIELD
    }
    public static class can_upload_image_result {
        public final boolean can_upload;
        public final String repo_name;
        public final BigDecimal required_ownership;
        public final BigDecimal user_ownership;
        public final enum_upload_image_policy_result enum_result;
        public can_upload_image_result(
                final boolean p_can_upload,
                        final String p_repo_name,
                        final BigDecimal p_required_ownership,
                        final BigDecimal p_user_ownership,
                        final enum_upload_image_policy_result p_enum_result
        ) {
            can_upload = p_can_upload;
            repo_name = p_repo_name;
            required_ownership = p_required_ownership;
            user_ownership = p_user_ownership;
            enum_result = p_enum_result;
        }
    }


    public static can_upload_image_result can_upload_image(final String user_name, @Nonnull final String repo_name) {
        if (user_name == null) {
            return new can_upload_image_result(false, repo_name, null ,null, enum_upload_image_policy_result.NULL_NAME);
        }
        assert repo_name != null;
        final model_ownership ownership = store_local_db.get_ownership_by_user_name_and_repo_name(user_name, repo_name);
        if (ownership==null) {
            return new can_upload_image_result(false, repo_name, null ,null, enum_upload_image_policy_result.NULL_OWNERHSIP);
        }
        final model_repo_policy repo_policy = store_local_db.get_policy_by_repo(repo_name);
        if (repo_policy==null) {
            return new can_upload_image_result(false, repo_name, null ,null, enum_upload_image_policy_result.NO_REPO_POLICY);
        }
        boolean can_upload = ownership.percent.compareTo(repo_policy.ownership_required_to_manage_repo) >= 0;
        return new can_upload_image_result(can_upload, repo_name, repo_policy.ownership_required_to_manage_repo ,ownership.percent, enum_upload_image_policy_result.SEE_CAN_UPLOAD_FIELD);
    }

}
