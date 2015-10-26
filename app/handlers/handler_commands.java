package handlers;

import models.model_ownership;
import models.model_pull_request;
import models_github.interface_github_webhook;
import play.Logger;
import stores.store_github_api;
import stores.store_local_db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skariel on 17/10/15.
 */
public class handler_commands {

    public static ArrayList<String> handle_from_hook(interface_github_webhook hook) {
        // TODO: this works but seems a bit inefficient. Do all commands need to be checked against every comment?
        ArrayList<String> responses = new ArrayList<>();
        responses.add(command_list_owners(hook));
        responses.add(command_say_hi(hook));
        responses.add(command_merge(hook));
        return responses;
    }

    private static String command_list_owners(interface_github_webhook hook) {
        if (!hook.get_comment().contains("@theindiepocalypse list owners")) {
            return "";
        }
        String response = "Owner | Percent\n"+
                          "-------|---------\n";
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        for (model_ownership ownership: ownerships) {
            response += "@" + ownership.user.user_name + "|" + ownership.percent.toString()+"\n";
        }
        response += "*total* | 100.0\n";
        return response;
    }

    private static String command_say_hi(interface_github_webhook hook) {
        if (!hook.get_comment().contains("@theindiepocalypse say hi")) {
            return "";
        }
        return "hi!";
    }

    private static String command_merge(interface_github_webhook hook) {
        // TODO: take care of ownership changes!
        if (!hook.get_comment().contains("@theindiepocalypse merge")) {
            return "";
        }
        model_pull_request pull_request = hook.get_pull_request();
        if (pull_request==null) {
            return "merging commands are allowed only on pull requests, nothing to merge here :)";
        }
        // TODO: match n actual commit message!
        if (store_github_api.merge_pull_request(pull_request, "I did this!")) {
            return "merged!\nThe new ownership structure:\n"+command_list_owners(hook);
        }
        else {
            return "There was a problem while merging. Please contact suppoer [here \'s the link] or try again later...";
        }
    }
}
