package commands;

import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;
import stores.store_conf;
import stores.store_local_db;
import utils.utils_bigdecimal;

import java.math.BigDecimal;

/**
 * Created by skariel on 31/10/15.
 */
public class command_change_policy_policy implements interface_command {
    // TODO: maybe use just e.g. `change policy 30%` is policy twice for consistency worth it?

    @Override
    public boolean is_recognized(model_command command) {
        if ((command.command.equals("change")) && (command.args.size() == 3) &&
                (command.args.get(0).equals("policy")) &&
                (command.args.get(1).equals("policy"))) {
            try {
                // try to parse this
                BigDecimal new_percent = utils_bigdecimal.from_percent_or_number(command.args.get(2));
                if (new_percent.compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
                return new_percent.compareTo(new BigDecimal("100.0")) <= 0;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        model_repo_policy policy = store_local_db.get_policy_by_repo(hook.get_repo());
        if (policy != null) {
            model_ownership ownership = store_local_db.get_ownership_by_user_name_and_repo_name(hook.get_user(), hook.get_repo());
            BigDecimal min_ownership = policy.ownership_required_to_change_policy;
            if (ownership == null) {
                return "Only owners with more than " + min_ownership.toString() + "% ownership can change policy of this repo. You currently have no ownership at all...";
            }
            if (ownership.percent.compareTo(min_ownership) < 0) {
                return "Only owners with more than " + min_ownership.toString() + "% ownership can change policy of this repo. You currently have " + ownership.percent.toString() + "%";
            }
        }

        // change the policy!
        BigDecimal new_percent = utils_bigdecimal.from_percent_or_number(command.args.get(2));
        if (new_percent.compareTo(store_conf.get_policy_floor_ownership_required_to_change_policy()) < 0) {
            return "the ownership to manage policy must be above " +
                    store_conf.get_policy_floor_ownership_required_to_change_policy() + "%";
        }

        if (policy == null) {
            policy = new model_repo_policy(hook.get_repo());
            policy = policy.same_but_with_different_policy_to_change_policy(new_percent);
            store_local_db.update_policy(policy);
            return "policy created";
        }
        policy = policy.same_but_with_different_policy_to_change_policy(new_percent);
        store_local_db.update_policy(policy);
        return "policy changed";
    }

    @Override
    public String get_command_name() {
        return "CHANGE_POLICY_POLICY";
    }

    @Override
    public String get_command_help() {
        return "change policy policy 30%";
    }
}
