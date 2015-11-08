package commands;

import handlers.handler_commands;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_list_owners implements interface_command {
    // TODO: implement!
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("list")) && (command.args.size()==1) &&
                (command.args.get(0).equals("owners"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        return handler_commands.get_owners_good_looking_table(hook);
    }

    @Override
    public String get_command_name() {
        return "LIST OWNERS";
    }
}
