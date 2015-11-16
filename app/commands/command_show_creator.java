package commands;

import models_db_indie.model_ownership;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;
import stores.store_local_db;
import stores.store_session;

/**
 * Created by skariel on 31/10/15.
 */
public class command_show_creator implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        return (command.command.equals("show")) && (command.args.size() == 1) &&
                (command.args.get(0).equals("creator"));
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        model_ownership creator = store_local_db.get_creator_by_repo(hook.get_repo());
        if (creator==null) {
            return "this repo has no creator";
        }
        if (creator.user.user_name.equals(store_session.get_user_name())) {
            return "the creator of this repo is you!";
        }
        return "the creator of this repo is @"+creator.user.user_name;
    }

    @Override
    public String get_command_name() {
        return "LIST OWNERS";
    }
}
