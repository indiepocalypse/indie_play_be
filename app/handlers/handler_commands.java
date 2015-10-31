package handlers;

import models.model_admin;
import models.model_ownership;
import models.model_pull_request;
import models_github.interface_github_webhook;
import models_github.model_command;
import models_github.model_issue;
import models_github.model_webhook_pull_request_created_or_updated;
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
        // TODO: refactor this into self contained command classes!
        // this method returns responses to be shown to users.
        // all responses are encapsulated in a common header which includes the @user welcome or whateve
        ArrayList<String> responses = new ArrayList<>();
        boolean command_recognized = false;
        for (model_command command: model_command.from_text(hook.get_comment())) {
            Logger.info("--- command: " + command.command+" length="+Integer.toString(command.command.length()));
            for (String arg: command.args) {
                Logger.info("         arg: "+arg+" length="+Integer.toString(arg.length()));
            }

            switch (command.command) {
                case "hi":
                    if (command.args.size()==0) {
                        responses.add("hi!");
                        command_recognized = true;
                    }
                    break;
                case "list":
                    if ((command.args.size()==1) && (command.args.get(0).equals("owners"))) {
                        responses.add(get_owners_good_looking_table(hook));
                        command_recognized = true;
                    }
                    else if ((command.args.size()==1) && (command.args.get(0).equals("admins"))) {
                        responses.add(get_admins_good_looking_list(hook));
                        command_recognized = true;
                    }
                    break;
                case "merge":
                    String commit_message = "this is the default commit message!";
                    if ((hook.get_pull_request()!=null) && (hook.get_pull_request().title!=null)) {
                        commit_message = hook.get_pull_request().title;
                    }
                    if (!command.joined_args.equals("")) {
                        commit_message = command.joined_args;
                    }
                    responses.add(handle_merge(hook, commit_message, true)); // true is for update pull request and retry
                    command_recognized = true;
                    break;
                case "delete":
                    if ((command.args.size()==1) && (command.args.get(0).equals("repo"))) {
                        if (store_local_db.is_admin(hook.get_user().user_name)) {
                            if (store_github_api.delete_repo(hook.get_repo())) {
                                store_local_db.delete_repo(hook.get_repo());
                                responses.add("done!");
                                command_recognized = true;
                            }
                            else {
                                responses.add("problem deleting repo. Please contact staff");
                                command_recognized = true;
                                Logger.error("error deleting repo "+hook.get_repo().repo_name);
                            }
                        } else {
                            responses.add("only admins can delete a repository");
                            command_recognized = true;
                        }
                    }
                    break;
                case "close":
                    if (hook.get_pull_request()!=null) {
                        // we have a pull reuqest
                        model_pull_request pull_request = hook.get_pull_request();
                        if (pull_request.is_closed()) {
                            responses.add("this pull request is already closed");
                            command_recognized = true;
                            break;
                        }
                        pull_request.state = "closed";
                        if (store_github_api.update_pull_request(pull_request)) {
                            store_local_db.update_pull_request(pull_request);
                            command_recognized = true;
                        }
                        else {
                            Logger.error("could not close pull request #"+pull_request.number+" on repo "+pull_request.repo.repo_name);
                        }
                    }
                    else {
                        // we have an issue
                        model_issue issue = hook.get_issue();
                        if (issue.is_closed()) {
                            responses.add("this issue is already closed");
                            command_recognized = true;
                            break;
                        }
                        issue.state = "closed";
                        if (!store_github_api.update_issue(hook.get_repo(), issue)) {
                            Logger.error("could not close issue #"+issue.number+" on repo "+hook.get_repo().repo_name);
                            command_recognized = true;
                        }
                    }
                    break;
                case "open":
                    if (hook.get_pull_request()!=null) {
                        // we have a pull reuqest
                        model_pull_request pull_request = hook.get_pull_request();
                        if (!pull_request.is_closed()) {
                            responses.add("this pull request is already open");
                            command_recognized = true;
                            break;
                        }
                        pull_request.state = "open";
                        if (store_github_api.update_pull_request(pull_request)) {
                            store_local_db.update_pull_request(pull_request);
                            command_recognized = true;
                        }
                        else {
                            Logger.error("could not open pull request #"+pull_request.number+" on repo "+pull_request.repo.repo_name);
                        }
                    }
                    else {
                        // we have an issue
                        model_issue issue = hook.get_issue();
                        if (!issue.is_closed()) {
                            responses.add("this issue is already open");
                            command_recognized = true;
                            break;
                        }
                        issue.state = "open";
                        if (!store_github_api.update_issue(hook.get_repo(), issue)) {
                            Logger.error("could not open issue #"+issue.number+" on repo "+hook.get_repo().repo_name);
                            command_recognized = true;
                        }
                    }
                    break;
            }
        }
        if ((!command_recognized) && (hook.get_comment().contains("@theindiepocalypse"))) {
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
        if (admins.size()==0) {
            return "no admins at all, weird!";
        }
        boolean more_than_one_admin = admins.size()>1;
        boolean are_you_included = false;
        for (model_admin admin: admins) {
            if (hook.get_user().user_name.equals(admin.user.user_name)) {
                are_you_included = true;
                continue;
            }
            response += "@" + admin.user.user_name+" ";
        }
        if ((more_than_one_admin)&&(are_you_included)) {
            response += "and you, are admins";
        }
        else if ((!more_than_one_admin)&&(are_you_included)) {
            response = "you are the only admin";
        }
        else if ((more_than_one_admin)&&(!are_you_included)) {
            response = "are admins";
        }
        return response;
    }

    private static String handle_merge(interface_github_webhook hook, String commit_message, boolean should_try_to_update_from_github_if_not_mergeable) {
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
        if ((issue!=null) && (issue.is_closed())) {
            return "this pull request is closed, please reopen to merge";
        }
        if (pull_request.mergeable==null) {
            // was undecided by Github when DB entry was created. Lets update the record....
            String repo_name = hook.get_pull_request().repo.repo_name;
            String number = hook.get_pull_request().number;
            Logger.info("updating mergeable field for repo "+repo_name+" for PR#"+number);
            pull_request = store_github_api.get_pull_request_by_repo_by_number(repo_name, number);
            store_local_db.update_pull_request(pull_request);
            if (pull_request.mergeable==null) {
                Logger.info("     -- after update: mergeable=null");
                return "Cannot merge right now. Maybe our DB is not yet in sync with Github. Please try again later...";
            }
            else {
                Logger.info("     -- after update: mergeable="+Boolean.toString(pull_request.mergeable));
            }
        }
        if (!pull_request.mergeable) {
            return "this pull request is not mergeable automatically (the merge button) maybe a rebase will solve the issue?";
        }

        // try to merge!
        if (store_github_api.merge_pull_request(pull_request, commit_message)) {
            pull_request.merged = true;
            pull_request.mergeable = false; // TODO: should this actually change?
            store_local_db.update_pull_request(pull_request);
            return "merged!\nThe new ownership structure:\n\n"+get_owners_good_looking_table(hook);
        }
        else {
            return "Some problem with merging. Please try again later, or contant an admin";
        }
    }
}
