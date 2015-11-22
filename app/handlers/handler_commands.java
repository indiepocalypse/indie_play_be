package handlers;

import commands.interface_command;
import models_db_github.model_pull_request;
import models_db_indie.model_admin;
import models_db_indie.model_offer_for_merge;
import models_db_indie.model_ownership;
import models_db_indie.model_request_for_merge;
import models_memory_github.interface_github_webhook;
import models_memory_github.model_issue;
import models_memory_indie.model_command;
import org.reflections.Reflections;
import play.Logger;
import scala.collection.immutable.Range;
import stores.github_io_exception;
import stores.store_github_api;
import stores.store_local_db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by skariel on 17/10/15.
 */
public class handler_commands {

    private static ArrayList<interface_command> commands = null;

    public static ArrayList<String> handle_commands_from_hook(interface_github_webhook hook) {
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

    public static String get_negotiations_good_looking_table(interface_github_webhook hook) {
        // this makes a list of all offers and the request
        // TODO: show current offers satisfied (and total satisfied)
        // TODO: show current offers unsatisfies (and total unsatisfied)
        // TODO: show minimal requirements for merge
        if (hook.get_pull_request()==null) {
            return "this is not a pull request, there are no offers here";
        }
        String response = "\n\nOwner | Current offer\n" +
                "-------|---------\n";
        List<model_offer_for_merge> offers = store_local_db.get_offers_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        if (offers.size()==0) {
            return "thre are no offers";
        }
        BigDecimal total = new BigDecimal("0.0");
        for (model_offer_for_merge offer : offers) {
            response += "@" + offer.user.user_name + "|" + offer.amount_percent.toString() + "\n";
            total.add(offer.amount_percent);
        }
        response += "*total* | "+total.toString()+"\n";

        response += "\n\n";
        model_request_for_merge request = store_local_db.get_request_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        if (request==null) {
            response += "no requested percentage for merging yet\n";
        }
        else {
            response += "requested for merge "+request.amount_percent.toString()+"%\n";
        }

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
        boolean you_are_included = false;
        for (model_admin admin : admins) {
            if (hook.get_user().user_name.equals(admin.user.user_name)) {
                you_are_included = true;
                continue;
            }
            response += "@" + admin.user.user_name + " ";
        }
        if ((more_than_one_admin) && (you_are_included)) {
            response += "and you, are admins";
        } else if ((!more_than_one_admin) && (you_are_included)) {
            response = "you are the only admin";
        } else if (more_than_one_admin) {
            response += "are admins";
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
        try {
            store_github_api.merge_pull_request(pull_request, commit_message);
            pull_request.merged = true;
            pull_request.mergeable = false; // TODO: should this actually change?
            handler_general.update_pull_request_and_clear_offers_if_necessary(pull_request);
            return "merged!\nThe new ownership structure:\n\n" + get_owners_good_looking_table(hook);
        }
        catch (github_io_exception e) {
            return "Some problem with merging. Please try again later, or contant an admin";
        }
    }

    public static String handle_make_request(interface_github_webhook hook, String percent_amount) {
        if (hook.get_pull_request()==null) {
            return "this is no pull request, cannot make a request for merge here";
        }
        if (!hook.get_user().user_name.equals(hook.get_pull_request().user.user_name)) {
            return "only the user making the pull request can make a request for merge";
        }
        if (hook.get_pull_request().merged) {
            return "this pull request is already merged, cannot place a request for merge";
        }
        if (hook.get_pull_request().is_closed()) {
            return "this pull request is closed, cannot place a request for merge";
        }
        model_pull_request pull_request = hook.get_pull_request();
        handler_general.update_pull_request_and_clear_offers_if_necessary(pull_request);
        if (hook.get_pull_request().mergeable==null) {
            return "cannot determine mergeability of pull request. Please try again later";
        }
        if (!pull_request.mergeable) {
            return "this pull request is not currently mergeable. Cannot place a request for merge";
        }

        // we can make or update the user request...

        model_request_for_merge current_request = store_local_db.get_request_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        final boolean is_active = true;
        final boolean was_positively_accepted = false;
        if (current_request!=null) {
            current_request = new model_request_for_merge(
                    current_request.user,
                    pull_request,
                    new BigDecimal(percent_amount),
                    is_active,
                    was_positively_accepted,
                    current_request.date_created,
                    current_request.date_accepted_if_accepted
            );
            store_local_db.update_request(current_request);
            return "request for merge updated to "+percent_amount+"%";
        }
        else {
            final Date date_accepted_if_accepted = null;
            final Date date_created = new Date();
            current_request = new model_request_for_merge(
                    hook.get_user(), pull_request, new BigDecimal(percent_amount),
                    is_active, was_positively_accepted, date_created, date_accepted_if_accepted);
            store_local_db.update_request(current_request);
            return "request for merge created as "+percent_amount+"%";
        }
    }

    public static String handle_make_offer(interface_github_webhook hook, String percent_amount) {
        if (hook.get_pull_request()==null) {
            return "this is no pull request, cannot make an offer for merge";
        }
        if (hook.get_user().user_name.equals(hook.get_pull_request().user.user_name)) {
            return "only users not creating the pull request can make an offer for merge";
        }
        if (hook.get_pull_request().merged) {
            return "this pull request is already merged, cannot place an offer";
        }
        if (hook.get_pull_request().is_closed()) {
            return "this pull request is closed, cannot place an offer";
        }
        model_pull_request pull_request = hook.get_pull_request();
        handler_general.update_pull_request_and_clear_offers_if_necessary(pull_request);
        if (hook.get_pull_request().mergeable==null) {
            return "cannot determine mergeability of pull request. Please try again later";
        }
        if (!pull_request.mergeable) {
            return "this pull request is not currently mergeable. Cannot place an offer for merge";
        }

        // we can make or update the user offer...

        model_offer_for_merge current_offer = store_local_db.get_offer_by_user_by_pull_request(hook.get_user().user_name, hook.get_repo().repo_name, hook.get_issue_num());
        final boolean is_active = true;
        final boolean was_positively_accepted = false;
        if (current_offer!=null) {
            current_offer = new model_offer_for_merge(
                    current_offer.user,
                    pull_request,
                    new BigDecimal(percent_amount),
                    is_active,
                    was_positively_accepted,
                    current_offer.date_created,
                    current_offer.date_accepted_if_accepted
            );
            store_local_db.update_offer(current_offer);
            return "request for merge updated to "+percent_amount+"%";
        }
        else {
            final Date date_accepted_if_accepted = null;
            final Date date_created = new Date();
            current_offer = new model_offer_for_merge(
                    hook.get_user(), pull_request, new BigDecimal(percent_amount),
                    is_active, was_positively_accepted, date_created, date_accepted_if_accepted);
            store_local_db.update_offer(current_offer);
            return "request for merge created as "+percent_amount+"%";
        }
    }

    public static String get_commands_good_looking_list(interface_github_webhook hook) {
        String result = "";
        for (interface_command command: commands) {
            result += command.get_command_help()+"\n";
        }
        return result;
    }
}
