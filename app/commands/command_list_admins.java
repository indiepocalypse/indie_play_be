package commands;

import handlers.handler_commands;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_list_admins implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("list")) && (command.args.size() == 1) &&
                (command.args.get(0).equals("admins"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        return handler_commands.get_admins_good_looking_list(hook);
    }

    @Override
    public String get_command_name() {
        return "LIST_ADMINS";
    }

    @Override
    public String get_command_help() {
        return "list admins";
    }

}
