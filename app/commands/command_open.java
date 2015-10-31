package commands;

import models.model_pull_request;
import models_github.interface_github_webhook;
import models_github.model_command;
import models_github.model_issue;
import play.Logger;
import stores.store_github_api;
import stores.store_local_db;

/**
 * Created by skariel on 31/10/15.
 */
public class command_open implements interface_command {
    // TODO: implement!
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("open")) && (command.args.size()==0);
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        if (hook.get_pull_request()!=null) {
            // we have a pull reuqest
            model_pull_request pull_request = hook.get_pull_request();
            if (pull_request.is_closed()) {
                return "this pull request is open";
            }
            pull_request.state = "open";
            if (store_github_api.update_pull_request(pull_request)) {
                store_local_db.update_pull_request(pull_request);
            }
            else {
                Logger.error("could not open pull request #"+pull_request.number+" on repo "+pull_request.repo.repo_name);
            }
        }
        else {
            // we have an issue
            model_issue issue = hook.get_issue();
            if (issue.is_closed()) {
                return "this issue is open";
            }
            issue.state = "open";
            if (!store_github_api.update_issue(hook.get_repo(), issue)) {
                Logger.error("could not open issue #"+issue.number+" on repo "+hook.get_repo().repo_name);
            }
        }
        return "";
    }

    @Override
    public String get_command_name() {
        return "OPEN";
    }
}
