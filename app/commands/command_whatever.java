package commands;

import models_github.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public class command_whatever implements interface_command {
    // TODO: implement!
    @Override
    public boolean is_recognized(model_command command) {
        return false;
    }

    @Override
    public String handle(model_command command) {
        return null;
    }

    @Override
    public String get_command_name() {
        return "--- this is command WHATEVER --- (just testing this out!";
    }
}
