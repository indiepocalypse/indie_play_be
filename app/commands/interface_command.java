package commands;

import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

/**
 * Created by skariel on 31/10/15.
 */
public interface interface_command {
    boolean is_recognized(model_command command);
    String handle(model_command command, interface_github_webhook hook);
    String get_command_name();
}
