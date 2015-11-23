package commands;

import handlers.handler_commands;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_list_commands implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("list")) && (command.args.size() == 1) &&
                (command.args.get(0).equals("commands"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        String result = "the commands are listed in the following examples:\n" +
                handler_commands.get_commands_good_looking_list(hook);
        return result;
    }

    @Override
    public String get_command_name() {
        return "LIST_COMMANDS";
    }

    @Override
    public String get_command_help() {
        return "list commands";
    }

}
