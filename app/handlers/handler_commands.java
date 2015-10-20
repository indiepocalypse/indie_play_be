package handlers;

import models.model_ownership;
import models_commands.*;
import models_github.interface_github_webhook;
import stores.store_github_api;
import stores.store_local_db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skariel on 17/10/15.
 */
public class handler_commands {

    public static ArrayList<String> handle_from_hook(interface_github_webhook hook) {
        ArrayList<String> responses = new ArrayList<>();
        responses.add(command_list_owners(hook));
        return responses;
    }

    private static String command_list_owners(interface_github_webhook hook) {
        if (!hook.get_comment().contains("@indiepocalypse list owners")) {
            return "";
        }
        String response = "Owner | Percent\n"+
                          "-----------------\n";
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        for (model_ownership ownership: ownerships) {
            response += ownership.user.user_name + "|" + ownership.percent.toString()+"\n";
        }
        return response;
    }

    private static String command_say_hi(interface_github_webhook hook) {
        if (!hook.get_comment().contains("@indiepocalypse say hi")) {
            return "";
        }
        return "hi!";
    }
}
