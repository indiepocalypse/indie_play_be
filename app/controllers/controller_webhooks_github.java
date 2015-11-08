package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import handlers.handler_commands;
import handlers.handler_general;
import models_memory_github.*;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import stores.store_github_api;
import stores.store_local_db;

import java.util.ArrayList;

/**
 * Created by skariel on 12/10/15.
 */
public class controller_webhooks_github extends Controller {

    public Result handle_wildcard() {
        // TODO: remove excessive logging.info and add datetime to log entries
        // TODO: consider using the github api package for java. See here: http://github-api.kohsuke.org/

        // basically, this whole class just gets a webhook, then extract commands from it,
        // runs them, and responses to the user that issued the commands (for e.g. on a comment)

        Logger.info("** incomming webhook! **");
        JsonNode json = request().body().asJson();

        final String sender_name = json.get("sender").get("login").asText();
        if (sender_name.equals("theindiepocalypse")) {
            // we don't want to be responding to ourselves
            return ok();
        }

        String response_header = "";
        String response_footer = "";

        // Extracting the hook

        interface_github_webhook hook;
        if (model_webhook_issue_comment_created.is_me(json)) {
            hook = model_webhook_issue_comment_created.from_json(json);
        } else if (model_webhook_pull_request_comment_created.is_me(json)) {
            hook = model_webhook_pull_request_comment_created.from_json(json);
        } else if (model_webhook_issue_created.is_me(json)) {
            hook = model_webhook_issue_created.from_json(json);
        } else if (model_webhook_pull_request_created_or_updated.is_me(json)) {
            hook = model_webhook_pull_request_created_or_updated.from_json(json);
            if (handler_general.update_pull_request_and_clear_offers_if_necessary(hook.get_pull_request())) {
                // code in PR was updated. No comment was created, no command issued.
                // store call was responsible to notify everybody
                return ok();
            }
        } else {
            Logger.info("we got some weird hook, not handled yet");
            return ok();
        }
        store_local_db.update_hook_components(hook);

        // running the commands

        ArrayList<String> command_responses = handler_commands.handle_from_hook(hook);

        // assembling response

        String response = response_header;
        if (!response.trim().equals("")) {
            response += "\n\n";
        }
        for (String command_response : command_responses) {
            response += command_response + "\n\n";
        }
        response += response_footer;

        if (response.trim().length() == 0) {
            return ok();
        }
        response = "@" + sender_name + ": " + response;
        if (!store_github_api.comment_on_issue(hook.get_repo(), hook.get_issue_num(), response)) {
            Logger.info("problem commenting...");
        }
        return ok();
    }
}

