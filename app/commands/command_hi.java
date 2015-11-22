package commands;

import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_hi implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("hi")) && (command.args.size() == 0);
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        return "hi!";
    }

    @Override
    public String get_command_name() {
        return "HI";
    }

    @Override
    public String get_command_help() {
        return "hi";
    }

}
