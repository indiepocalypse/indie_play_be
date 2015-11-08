package commands;

import models_db_github.model_pull_request;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;
import models_memory_github.model_issue;
import play.Logger;
import stores.store_github_api;
import stores.store_local_db;

/**
 * Created by skariel on 31/10/15.
 */
public class command_close implements interface_command {
    // TODO: implement!
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("close")) && (command.args.size()==0);
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        if (hook.get_pull_request()!=null) {
            // we have a pull reuqest
            model_pull_request pull_request = hook.get_pull_request();
            if (pull_request.is_closed()) {
                return "this pull request is already closed";
            }
            pull_request.state = "closed";
            if (store_github_api.update_pull_request(pull_request)) {
                store_local_db.update_pull_request(pull_request);
            }
            else {
                Logger.error("could not close pull request #"+pull_request.number+" on repo "+pull_request.repo.repo_name);
            }
        }
        else {
            // we have an issue
            model_issue issue = hook.get_issue();
            if (issue.is_closed()) {
                return "this issue is already closed";
            }
            issue.state = "closed";
            if (!store_github_api.update_issue(hook.get_repo(), issue)) {
                Logger.error("could not close issue #"+issue.number+" on repo "+hook.get_repo().repo_name);
            }
        }
        return "";
    }

    @Override
    public String get_command_name() {
        return "CLOSE";
    }
}
