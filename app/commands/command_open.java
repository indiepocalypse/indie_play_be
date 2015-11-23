package commands;

import handlers.handler_general;
import models_db_github.model_pull_request;
import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import models_memory_github.interface_github_webhook;
import models_memory_github.model_issue;
import models_memory_indie.model_command;
import play.Logger;
import stores.github_io_exception;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;

import java.math.BigDecimal;

/**
 * Created by skariel on 31/10/15.
 */
public class command_open implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("open")) && (command.args.size() == 0);
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        model_repo_policy policy = store_local_db.get_policy_by_repo(hook.get_repo());
        if (policy != null) {
            model_ownership ownership = store_local_db.get_ownerships_by_user_name_and_repo_name(hook.get_user(), hook.get_repo());
            BigDecimal min_ownership = policy.ownership_required_to_manage_issues;
            if (ownership == null) {
                return "Only owners with more than " + min_ownership.toString() + "% ownership can reopen issues. You currently have no ownership at all...";
            }
            if (ownership.percent.compareTo(store_conf.get_policy_default_ownership_required_to_manage_issues()) < 0) {
                return "Only owners with more than " + min_ownership.toString() + "% ownership can reopen issues. You currently have " + ownership.percent.toString() + "%";
            }
        }

        if (hook.get_pull_request() != null) {
            // we have a pull reuqest
            model_pull_request pull_request = hook.get_pull_request();
            if (!pull_request.is_closed()) {
                return "this pull request is open";
            }
            pull_request.state = "open";
            try {
                store_github_api.update_pull_request(pull_request);
                handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);
            } catch (github_io_exception e) {
                Logger.error("could not open pull request #" + pull_request.number + " on repo " + pull_request.repo.repo_name);
            }
        } else {
            // we have an issue
            model_issue issue = hook.get_issue();
            if (!issue.is_closed()) {
                return "this issue is open";
            }
            issue.state = "open";
            try {
                store_github_api.update_issue(hook.get_repo(), issue);
            } catch (github_io_exception e) {
                Logger.error("could not open issue #" + issue.number + " on repo " + hook.get_repo().repo_name);
            }
        }
        return "";
    }

    @Override
    public String get_command_name() {
        return "OPEN";
    }

    @Override
    public String get_command_help() {
        return "open";
    }

}
