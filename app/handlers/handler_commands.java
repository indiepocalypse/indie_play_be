package handlers;

import commands.interface_command;
import models_db_github.model_pull_request;
import models_db_indie.model_admin;
import models_db_indie.model_offer;
import models_db_indie.model_ownership;
import models_memory_github.interface_github_webhook;
import models_memory_github.model_issue;
import models_memory_indie.model_command;
import org.reflections.Reflections;
import play.Logger;
import stores.store_github_api;
import stores.store_local_db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Created by skariel on 17/10/15.
 */
public class handler_commands {

    private static ArrayList<interface_command> commands = null;

    public static ArrayList<String> handle_from_hook(interface_github_webhook hook) {
        if (commands == null) {
            dynamically_initialize_commands();
        }

        // this method returns responses to be shown to users.
        // all responses are encapsulated in a common header which includes the @user welcome or whateve
        ArrayList<String> responses = new ArrayList<>();
        boolean some_command_recognized = false;
        for (model_command command : model_command.from_text(hook.get_comment())) {
            Logger.info("--- command: " + command.command + " length=" + Integer.toString(command.command.length()));
            for (String arg : command.args) {
                Logger.info("         arg: " + arg + " length=" + Integer.toString(arg.length()));
            }

            for (interface_command command_handler : commands) {
                if (command_handler.is_recognized(command)) {
                    responses.add(command_handler.handle(command, hook));
                    some_command_recognized = true;
                    break;
                }
            }
        }
        if ((!some_command_recognized) && (hook.get_comment().contains("@theindiepocalypse"))) {
            // @theindipocalypse was mentioned but no command was parsed!
            // TODO: give actual help (say, a link to the help page?)
            // TODO: this will show up only if no command was recognized. What if some were but some weren't?
            responses.add("such command... much help please");
        }
        return responses;
    }

    private static void dynamically_initialize_commands() {
        Logger.info("dynamically initializing commands");
        commands = new ArrayList<>(100);
        Reflections reflections = new Reflections("commands");
        for (Class command : reflections.getSubTypesOf(interface_command.class)) {
            try {
                commands.add((interface_command) command.newInstance());
            } catch (Exception e) {
                Logger.error("while dynamically loading commands", e);
            }
        }
        // just testing
        for (interface_command command : commands) {
            Logger.info("found command: " + command.get_command_name());
        }
    }

    public static String get_owners_good_looking_table(interface_github_webhook hook) {
        String response = "\n\nOwner | Percent\n" +
                "-------|---------\n";
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        if (ownerships.size()==0) {
            return "thre are no owners";
        }
        for (model_ownership ownership : ownerships) {
            response += "@" + ownership.user.user_name + "|" + ownership.percent.toString() + "\n";
        }
        response += "*total* | 100.0\n";
        return response;
    }

    public static String get_offers_good_looking_table(interface_github_webhook hook) {
        // TODO: show requested percent
        // TODO: show current offers satisfied (and total satisfied)
        // TODO: show current offers unsatisfies (and total unsatisfied)
        // TODO: show minimal requirements for merge
        String response = "\n\nOwner | Current offer\n" +
                "-------|---------\n";
        List<model_offer> offers = store_local_db.get_offers_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        if (offers.size()==0) {
            return "thre are no offers";
        }
        BigDecimal total = new BigDecimal("0.0");
        for (model_offer offer : offers) {
            response += "@" + offer.user.user_name + "|" + offer.amount_percent.toString() + "\n";
            total.add(offer.amount_percent);
        }
        response += "*total* | "+total.toString()+"\n";
        return response;
    }

    public static String get_admins_good_looking_list(interface_github_webhook hook) {
        // TODO: improve response wording, etc.
        String response = " ";
        List<model_admin> admins = store_local_db.get_all_admins();
        if (admins.size() == 0) {
            return "there are no admins";
        }
        boolean more_than_one_admin = admins.size() > 1;
        boolean are_you_included = false;
        for (model_admin admin : admins) {
            if (hook.get_user().user_name.equals(admin.user.user_name)) {
                are_you_included = true;
                continue;
            }
            response += "@" + admin.user.user_name + " ";
        }
        if ((more_than_one_admin) && (are_you_included)) {
            response += "and you, are admins";
        } else if ((!more_than_one_admin) && (are_you_included)) {
            response = "you are the only admin";
        } else if (more_than_one_admin) {
            response = "are admins";
        }
        return response;
    }

    public static String handle_merge(interface_github_webhook hook, String commit_message) {
        // TODO: take care of ownership changes!
        // we have a merge command!
        // check whether its mergeable:
        model_pull_request pull_request = hook.get_pull_request();
        if (pull_request == null) {
            return "merging commands are allowed only on pull requests, nothing to merge here :)";
        }
        if (pull_request.merged) {
            return "this pull request is already merged!";
        }
        model_issue issue = hook.get_issue();
        if ((issue != null) && (issue.is_closed())) {
            return "this pull request is closed, please reopen to merge";
        }
        // always update pull request before merge! so we have offers/pull version aligned!
        String repo_name = hook.get_pull_request().repo.repo_name;
        String number = hook.get_pull_request().number;
        Logger.info("updating pull request #" + number + " for repo " + repo_name);
        pull_request = store_github_api.get_pull_request_by_repo_by_number(repo_name, number);
        handler_general.update_pull_request_and_clear_offers_if_necessary(pull_request);

        if (pull_request.mergeable == null) {
            Logger.info("     -- pull request.mergeable==null");
            return "Cannot merge right now. Maybe our DB is not yet in sync with Github. Please try again later...";
        } else {
            Logger.info("     -- mergeable=" + Boolean.toString(pull_request.mergeable));
        }

        if (!pull_request.mergeable) {
            return "this pull request is not mergeable automatically (the merge button) maybe a rebase will solve the issue?";
        }

        // try to merge!
        if (store_github_api.merge_pull_request(pull_request, commit_message)) {
            pull_request.merged = true;
            pull_request.mergeable = false; // TODO: should this actually change?
            handler_general.update_pull_request_and_clear_offers_if_necessary(pull_request);
            return "merged!\nThe new ownership structure:\n\n" + get_owners_good_looking_table(hook);
        } else {
            return "Some problem with merging. Please try again later, or contant an admin";
        }
    }
}
