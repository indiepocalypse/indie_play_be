package commands;

import handlers.handler_general;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;
import play.Logger;
import stores.github_io_exception;
import stores.store_github_api;
import stores.store_local_db;

/**
 * Created by skariel on 31/10/15.
 */
public class command_delete implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("delete")) &&
                (command.args.size() == 1) &&
                (command.args.get(0).equals("repo"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        if (store_local_db.is_admin(hook.get_user().user_name)) {
            try {
                store_github_api.delete_repo(hook.get_repo());
                handler_general.delete_repo_from_github_and_db_and_also_related_ownership_policy_offers(hook.get_repo());
                return "done!";
            }
            catch (github_io_exception e) {
                Logger.error("error deleting repo " + hook.get_repo().repo_name);
                return "problem deleting repo. Please contact staff";
            }
        } else {
            return "only admins can delete a repository";
        }
    }

    @Override
    public String get_command_name() {
        return "DELETE REPO";
    }

    @Override
    public String get_command_help() {
        return "delete repo";
    }

}
