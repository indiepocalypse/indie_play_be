package commands;

import models_github.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public interface interface_command {
    boolean is_recognized(model_command command);
    String handle(model_command command);
    String get_command_name();
}
