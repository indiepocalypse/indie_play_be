package handlers;

import models.model_ownership;
import models.model_pull_request;
import models_github.interface_github_webhook;
import models_github.model_command;
import models_github.model_issue;
import stores.store_github_api;
import stores.store_local_db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skariel on 17/10/15.
 */
public class handler_commands {
    public static ArrayList<String> handle_from_hook(interface_github_webhook hook) {
        // this method returns responses to be shown to users.
        // all responses are encapsulated in a common header which includes the @user welcome or whateve
        // TODO: this works but seems a bit inefficient. Do all commands need to be checked against every comment?
        ArrayList<String> responses = new ArrayList<>();
        for (model_command command: model_command.from_text(hook.get_comment())) {

            switch (command.command) {
                case "hi":
                    if (command.args.size()==0) {
                        responses.add("hi!");
                    }
                    break;
                case "list":
                    if ((command.args.size()==1) && (command.args.get(0).equals("owners"))) {
                        responses.add(get_owners_good_looking_table(hook));
                    }
                    break;
                case "merge":
                    if (command.args.size()==0) {
                        responses.add(handle_merge(hook));
                    }
                    break;
            }
        }
        if ((responses.size()==0) && (hook.get_comment().contains("@theindiepocalypse"))) {
            // @theindipocalypse was mentioned but no command was parsed!
            // TODO: give actual help (say, a link to the help page?)
            responses.add("such command... much help please");
        }
        return responses;
    }

    private static String get_owners_good_looking_table(interface_github_webhook hook) {
        String response = "Owner | Percent\n"+
                "-------|---------\n";
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        for (model_ownership ownership: ownerships) {
            response += "@" + ownership.user.user_name + "|" + ownership.percent.toString()+"\n";
        }
        response += "*total* | 100.0\n";
        return response;
    }

    private static String handle_merge(interface_github_webhook hook) {
        // TODO: take care of ownership changes!
        // we have a merge command!
        // check whether its mergeable:
        model_pull_request pull_request = hook.get_pull_request();
        if (pull_request==null) {
            return "merging commands are allowed only on pull requests, nothing to merge here :)";
        }
        if (pull_request.merged) {
            return "this pull request is already merged!";
        }
        model_issue issue = hook.get_issue();
        if ((issue!=null) && (issue.is_closed)) {
            return "this pull request is closed, please reopen to merge";
        }
        if (!pull_request.mergeable) {
            return "this pull request is not mergeable automatically (the merge button) maybe a rebase will solve the issue? I can only merge with the merge button...";
        }
        // TODO: match actual commit message!
        if (store_github_api.merge_pull_request(pull_request, "I did this!")) {
            return "merged!\nThe new ownership structure:\n\n"+get_owners_good_looking_table(hook);
        }
        else {
            return "There was a problem while merging. Please contact suppoer [here \'s the link] or try again later...";
        }
    }
}
