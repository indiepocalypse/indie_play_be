package commands;

import handlers.handler_commands;
import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;
import stores.store_conf;
import stores.store_local_db;

import java.math.BigDecimal;

/**
 * Created by skariel on 31/10/15.
 */
public class command_list_offers implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("list")) && (command.args.size() == 1) &&
                (command.args.get(0).equals("offers"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        return handler_commands.get_offers_good_looking_table(hook);
    }

    @Override
    public String get_command_name() {
        return "LIST_OFFERS";
    }
}
