package handlers;

import commands.interface_command;
import models_db_github.model_pull_request;
import models_db_indie.*;
import models_memory_github.interface_github_webhook;
import models_memory_github.model_issue;
import models_memory_indie.model_command;
import org.reflections.Reflections;
import play.Logger;
import stores.github_io_exception;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;
import utils.utils_bigdecimal;

import javax.annotation.Nonnull;
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
        dynamically_initialize_commands();

        // this method returns responses to be shown to users.
        // all responses are encapsulated in a common header which includes the @user welcome or whatever
        ArrayList<String> responses = new ArrayList<>();
        boolean some_command_recognized = false;
        for (model_command command : model_command.from_text(hook.get_comment())) {
            Logger.info("--- command: " + command.command + " length=" + Integer.toString(command.command.length()));
            for (String arg : command.args) {
                Logger.info("         arg: " + arg + " length=" + Integer.toString(arg.length()));
            }

            int recognized_commands_in_comment = 0;
            for (interface_command command_handler : commands) {
                if (command_handler.is_recognized(command)) {
                    recognized_commands_in_comment += 1;
                    if (recognized_commands_in_comment > store_conf.get_rate_limit_maximum_commands_per_comment()) {
                        responses.add("Maximum " + Integer.toString(store_conf.get_rate_limit_maximum_commands_per_comment()) +
                                "commands per comment");
                        return responses;
                    }
                    responses.add(command_handler.handle(command, hook));
                    // registering interaction
                    model_user_interaction model_user_interaction = models_db_indie.model_user_interaction.from_general_command(command_handler, hook);
                    store_local_db.update_user_interaction(model_user_interaction);
                    some_command_recognized = true;
                    break;
                }
            }
        }
        if ((!some_command_recognized) && (hook.get_comment() != null) && (hook.get_comment().contains("@theindiepocalypse"))) {
            // @theindipocalypse was mentioned but no command was parsed!
            // TODO: give actual help (say, a link to the help page?)
            // TODO: this will show up only if no command was recognized. What if some were but some weren't?
            responses.add("such command... much help please");
        }
        return responses;
    }

    private static void dynamically_initialize_commands() {
        if (commands == null) {
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
            // just for debug
            for (interface_command command : commands) {
                Logger.info("found command: " + command.get_command_name());
            }
        }
    }

    public static String get_owners_good_looking_table(interface_github_webhook hook) {
        String response = "\n\nOwner | Percent\n" +
                "-------|---------\n";
        List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        if (ownerships.size() == 0) {
            return "thre are no owners";
        }
        for (model_ownership ownership : ownerships) {
            response += "@" + ownership.user_name + "|" + ownership.percent.toString() + "\n";
        }
        response += "*total* | 100.0\n";
        return response;
    }

    public static String get_negotiations_good_looking_table(interface_github_webhook hook) {
        // TODO: use the negotiation status class
        if (hook.get_pull_request() == null) {
            return "this is not a pull request, there are no offers here";
        }

        String request_str;
        model_request_for_merge request = store_local_db.get_request_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        if (request == null) {
            request_str = "no requested percentage for merging yet\n";
        } else {
            request_str = "requested for merge " + request.amount_percent.toString() + "%\n";
        }

        String offers_str = "\n\nOwner | Current offer\n" +
                "-------|---------\n";
        List<model_offer_for_merge> offers = store_local_db.get_offers_by_pull_request(hook.get_pull_request());
        if (offers.size() == 0) {
            return request_str + "thre are no offers";
        }
        BigDecimal total = new BigDecimal("0.0");
        for (model_offer_for_merge offer : offers) {
            offers_str += "@" + offer.user_name + "|" + offer.amount_percent.toString() + "\n";
            total = total.add(offer.amount_percent);
        }
        offers_str += "*total* | " + total.toString() + "\n";

        return request_str + offers_str;
    }

    public static String get_admins_good_looking_list(interface_github_webhook hook) {
        // TODO: improve response wording, etc.
        String response = " ";
        List<model_user_extended_info> admins = store_local_db.get_all_admins();
        if (admins.size() == 0) {
            return "there are no admins";
        }
        boolean more_than_one_admin = admins.size() > 1;
        boolean you_are_included = false;
        for (model_user_extended_info admin : admins) {
            if (hook.get_user().user_name.equals(admin.user_name)) {
                you_are_included = true;
                continue;
            }
            response += "@" + admin.user_name + " ";
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

    private static String handle_merge(interface_github_webhook hook, negotiation_status negotiation_status) {
        // TODO: Auto generate copyrights file on each transaction, etc.
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
        String repo_name = hook.get_pull_request().repo_name;
        String number = hook.get_pull_request().number;
        Logger.info("updating pull request #" + number + " for repo " + repo_name);

        try {
            pull_request = store_github_api.get_pull_request_by_repo_by_number(repo_name, number);
        } catch (github_io_exception e) {
            return "cannot get updated pull request. cannot merge";
        }
        handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);

        if (pull_request.mergeable == null) {
            Logger.info("     -- pull request.mergeable==null");
            return "Cannot merge right now. Maybe our DB is not yet in sync with Github. Please try again later...";
        } else {
            Logger.info("     -- mergeable=" + Boolean.toString(pull_request.mergeable));
        }

        if (!pull_request.mergeable) {
            return "this pull request is not mergeable automatically (the merge button) maybe a rebase will solve the issue?";
        }

        // extract commit message
        @Nonnull final String commit_message = hook.get_pull_request().title;

        // try to merge!
        try {
            store_github_api.merge_pull_request(pull_request, commit_message);
            pull_request = pull_request.same_but_merged();
            handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);
            handler_general.consume_succesful_negotiation(negotiation_status);
            return "merged!\nThe new ownership structure:\n\n" + get_owners_good_looking_table(hook);
        } catch (github_io_exception e) {
            return "Some problem with merging. Please try again later, or contant an admin";
        }
    }

    public static String handle_make_request(interface_github_webhook hook, String percent_amount) {
        percent_amount = percent_amount.replace("%", "");
        if (hook.get_pull_request() == null) {
            return "this is no pull request, cannot make a request for merge here";
        }
        if (!hook.get_user().user_name.equals(hook.get_pull_request().user_name)) {
            return "only the user making the pull request can make a request for merge";
        }
        if (hook.get_pull_request().merged) {
            return "this pull request is already merged, cannot place a request for merge";
        }
        if (hook.get_pull_request().is_closed()) {
            return "this pull request is closed, cannot place a request for merge";
        }
        model_pull_request pull_request = hook.get_pull_request();


        try {
            pull_request = store_github_api.get_pull_request_by_repo_by_number(pull_request.repo_name, pull_request.number);
        } catch (github_io_exception e) {
            return "cannot get updated pull request. cannot open request for merge";
        }

        handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);
        if (pull_request.mergeable == null) {
            return "cannot determine mergeability of pull request. Please try again later";
        }
        if (!pull_request.mergeable) {
            return "this pull request is not currently mergeable. Cannot place a request for merge";
        }

        // we can make or update the user request...

        model_request_for_merge current_request = store_local_db.get_request_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());

        final boolean is_active = true;
        final boolean was_positively_accepted = false;
        String result;
        if (current_request != null) {
            current_request = new model_request_for_merge(
                    current_request.user_name,
                    pull_request.id,
                    utils_bigdecimal.from_percent_or_number(percent_amount),
                    is_active,
                    was_positively_accepted,
                    current_request.date_created,
                    current_request.date_accepted_if_accepted,
                    current_request.user_ownership_percent
            );
            store_local_db.update_request(current_request);
            result = "request for merge updated to " + percent_amount + "%";
        } else {
            final Date date_accepted_if_accepted = null;
            final Date date_created = new Date();
            final model_ownership user_ownership = store_local_db.get_ownership_by_user_name_and_repo_name(hook.get_user(), hook.get_repo());
            if (user_ownership != null) {
                current_request = new model_request_for_merge(
                        hook.get_user().user_name,
                        pull_request.id,
                        utils_bigdecimal.from_percent_or_number(percent_amount),
                        is_active,
                        was_positively_accepted,
                        date_created, date_accepted_if_accepted,
                        user_ownership.percent);
                store_local_db.update_request(current_request);
                result = "request for merge created as " + percent_amount + "%";
            } else {
                Logger.error("user that made request has no ownership, that should not happen");
                result = "some internal error ocurred. Please contact admins";
            }
        }

        final List<model_offer_for_merge> offers = store_local_db.get_offers_by_pull_request(hook.get_pull_request());
        final model_repo_policy policy = store_local_db.get_policy_by_repo(hook.get_repo());
        final List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        try {
            final negotiation_status negotiation_status = new negotiation_status(pull_request, current_request, offers, policy, ownerships, hook.get_repo());
            result += "\nnego status:\n\n" + negotiation_status.toString();
            if (negotiation_status.is_negotiation_succesful()) {
                result += "\nnegotiation succesful. Merging\n";
                result += "\n" + handle_merge(hook, negotiation_status) + "\n";
            }
        }
        catch (Exception e) {
            Logger.error("while handle_make_request, creation of negotiation_status failed: ", e);
            return "There a problem creating the negotiation status, please try again later or contact an admin";
        }
        return result;
    }

    // this is like handle_make_offer but offers an exact amount to accept the offer
    public static String handle_tailor_offer(interface_github_webhook hook) {
        final model_request_for_merge request = store_local_db.get_request_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        if (request!=null) {
            return handle_make_offer(hook, request.amount_percent.toString());
        }
        else {
            return "cannot find the request. Please try again later, or contact an admin";
        }
    }

    public static String handle_make_offer(interface_github_webhook hook, String percent_amount) {
        percent_amount = percent_amount.replace("%", "");
        if (hook.get_pull_request() == null) {
            return "this is no pull request, cannot make an offer for merge";
        }
        if (hook.get_user().user_name.equals(hook.get_pull_request().user_name)) {
            return "only users not creating the pull request can make an offer for merge";
        }
        if (hook.get_pull_request().merged) {
            return "this pull request is already merged, cannot place an offer";
        }
        if (hook.get_pull_request().is_closed()) {
            return "this pull request is closed, cannot place an offer";
        }
        final model_ownership user_ownership = store_local_db.get_ownership_by_user_name_and_repo_name(hook.get_user(), hook.get_repo());
        if ((user_ownership == null) ||
                (user_ownership.percent.compareTo(BigDecimal.ZERO) <= 0)) {
            return "Only owners with ownership can make offers";
        }

        model_pull_request pull_request = hook.get_pull_request();

        try {
            pull_request = store_github_api.get_pull_request_by_repo_by_number(pull_request.repo_name, pull_request.number);
        } catch (github_io_exception e) {
            return "cannot get updated pull request. cannot place offer";
        }
        handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);
        if (pull_request.mergeable == null) {
            return "cannot determine mergeability of pull request. Please try again later";
        }
        if (!pull_request.mergeable) {
            return "this pull request is not currently mergeable. Cannot place an offer for merge";
        }

        // we can make or update the user offer...

        model_offer_for_merge current_offer = store_local_db.get_offer_by_user_by_pull_request(hook.get_user().user_name, hook.get_pull_request());
        final boolean is_active = true;
        final boolean was_positively_accepted = false;
        String result;
        if (current_offer != null) {
            current_offer = new model_offer_for_merge(
                    current_offer.user_name,
                    pull_request.id,
                    utils_bigdecimal.from_percent_or_number(percent_amount),
                    is_active,
                    was_positively_accepted,
                    current_offer.date_created,
                    current_offer.date_accepted_if_accepted,
                    current_offer.user_ownership_percent
            );
            store_local_db.update_offer(current_offer);
            result = "offer for merge updated to " + percent_amount + "%";
        } else {
            final Date date_accepted_if_accepted = null;
            final Date date_created = new Date();
            current_offer = new model_offer_for_merge(
                    hook.get_user().user_name,
                    pull_request.id,
                    utils_bigdecimal.from_percent_or_number(percent_amount),
                    is_active,
                    was_positively_accepted,
                    date_created,
                    date_accepted_if_accepted,
                    user_ownership.percent);
            store_local_db.update_offer(current_offer);
            result = "offer for merge created as " + percent_amount + "%";
        }

        final model_request_for_merge request = store_local_db.get_request_by_pull_request(hook.get_repo().repo_name, hook.get_issue_num());
        final List<model_offer_for_merge> offers = store_local_db.get_offers_by_pull_request(hook.get_pull_request());
        final model_repo_policy policy = store_local_db.get_policy_by_repo(hook.get_repo());
        final List<model_ownership> ownerships = store_local_db.get_ownerships_by_repo_name(hook.get_repo().repo_name);
        try {
            final negotiation_status negotiation_status = new negotiation_status(pull_request, request, offers, policy, ownerships, hook.get_repo());
            result += "\nnego status:\n\n" + negotiation_status.toString();
            if (negotiation_status.is_negotiation_succesful()) {
                result += "\nnegotiation succesful. Merging\n";
                result += "\n" + handle_merge(hook, negotiation_status) + "\n";
            }
        }
        catch (Exception e) {
            Logger.error("while handle_make_offer, creation of negotiation_status failed: ", e);
            return "There a problem creating the negotiation status, please try again later or contact an admin";
        }
        return result;
    }

    public static String get_commands_good_looking_list() {
        String result = "";
        for (interface_command command : commands) {
            result += command.get_command_help() + "\n";
        }
        return result;
    }
}
