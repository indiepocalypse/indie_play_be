package commands;

import handlers.handler_commands;
import models_github.interface_github_webhook;
import models_github.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_merge implements interface_command {
    // TODO: implement!
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("merge")) && (command.args.size()==0);
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        String commit_message = "this is the default commit message!";
        if ((hook.get_pull_request()!=null) && (hook.get_pull_request().title!=null)) {
            commit_message = hook.get_pull_request().title;
        }
        if (!command.joined_args.equals("")) {
            commit_message = command.joined_args;
        }
        return handler_commands.handle_merge(hook, commit_message);
    }

    @Override
    public String get_command_name() {
        return "MERGE";
    }
}
