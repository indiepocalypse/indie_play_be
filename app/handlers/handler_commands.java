package handlers;

import models.model_admin;
import models.model_ownership;
import models.model_pull_request;
import models_github.interface_github_webhook;
import models_github.model_command;
import models_github.model_issue;
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
        // this method returns responses to be shown to users.
        // all responses are encapsulated in a common header which includes the @user welcome or whateve
        ArrayList<String> responses = new ArrayList<>();
        for (model_command command: model_command.from_text(hook.get_comment())) {
            Logger.info("--- command: " + command.command+" length="+Integer.toString(command.command.length()));
            for (String arg: command.args) {
                Logger.info("         arg: "+arg+" length="+Integer.toString(arg.length()));
            }

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
                    else if ((command.args.size()==1) && (command.args.get(0).equals("admins"))) {
                        responses.add(get_admins_good_looking_list(hook));
                    }
                    break;
                case "merge":
                    String commit_message = "this is the default commit message!";
                    if (command.joined_args.equals("")) {
                        commit_message = command.joined_args;
                    }
                    responses.add(handle_merge(hook, commit_message));
                    break;
                case "delete":
                    if ((command.args.size()==1) && (command.args.get(0).equals("repo"))) {
                        if (store_local_db.is_admin(hook.get_user().user_name)) {
                            if (store_github_api.delete_repo(hook.get_repo())) {
                                store_local_db.delete_repo(hook.get_repo());
                                responses.add("done!");
                            }
                            else {
                                responses.add("problem deleting repo. Please contact staff");
                                Logger.error("error deleting repo "+hook.get_repo().repo_name);
                            }
                        } else {
                            responses.add("only admins can delete a repository");
                        }
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
        String response = "\n\nOwner | Percent\n"+
                "-------|---------\n";
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        for (model_ownership ownership: ownerships) {
            response += "@" + ownership.user.user_name + "|" + ownership.percent.toString()+"\n";
        }
        response += "*total* | 100.0\n";
        return response;
    }

    private static String get_admins_good_looking_list(interface_github_webhook hook) {
        // TODO: improve response wording, etc.
        String response = " ";
        List<model_admin> admins = store_local_db.get_all_admins();
        for (model_admin admin: admins) {
            if (hook.get_user().user_name.equals(admin.user.user_name)) {
                response += "you ";
            }
            response += "@" + admin.user.user_name+" ";
        }
        response += " are admins";
        return response;
    }

    private static String handle_merge(interface_github_webhook hook, String commit_message) {
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
        if (store_github_api.merge_pull_request(pull_request, commit_message)) {
            return "merged!\nThe new ownership structure:\n\n"+get_owners_good_looking_table(hook);
        }
        else {
            return "There was a problem while merging. Please contact suppoer [here \'s the link] or try again later...";
        }
    }
}
