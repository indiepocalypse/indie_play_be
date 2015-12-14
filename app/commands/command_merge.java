package commands;

import handlers.handler_commands;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_merge implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("merge")) && (command.args.size() == 0);
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        return handler_commands.handle_tailor_offer(hook);
    }

    @Override
    public String get_command_name() {
        return "MERGE";
    }

    @Override
    public String get_command_help() {
        return "merge";
    }

}
