package commands;

import models_db_indie.model_repo_policy;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;
import stores.store_local_db;

/**
 * Created by skariel on 31/10/15.
 */
public class command_show_policy implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("show")) && (command.args.size() == 1)
                && (command.args.get(0).equals("policy"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        model_repo_policy policy = store_local_db.get_policies_by_repo(hook.get_repo());
        if (policy == null) {
            return "no policy for this repo";
        }
        String str = "";
        str += "minimal ownership for manaing issues is " + policy.ownership_required_to_manage_issues.toString() + "\n";
        str += "minimal ownership for changing policy is " + policy.ownership_required_to_change_policy.toString() + "\n";
        return str;
    }

    @Override
    public String get_command_name() {
        return "SHOW POLICY";
    }
}
