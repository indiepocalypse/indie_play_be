package commands;

import handlers.handler_commands;
import models_memory_github.interface_github_webhook;
import models_memory_indie.model_command;

import java.math.BigDecimal;

/**
 * Created by skariel on 31/10/15.
 */
public class command_offer implements interface_command {
    @Override
    public boolean is_recognized(model_command command) {
        if ((command.command.equals("offer")) && (command.args.size() == 1)) {
            try {
                // try to parse this
                BigDecimal percent = new BigDecimal(command.args.get(0));
                if (percent.compareTo(new BigDecimal("0.0"))<0) {
                    return false;
                }
                if (percent.compareTo(new BigDecimal("100.0"))>0) {
                    return false;
                }
                return true;
            }
            catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    @Override
    public String handle(model_command command, interface_github_webhook hook) {
        final String amount = command.args.get(0);
        return handler_commands.handle_make_offer(hook, amount);
    }

    @Override
    public String get_command_name() {
        return "OFFER";
    }

    @Override
    public String get_command_help() {
        return "offer 0.3";
    }
}
