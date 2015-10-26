package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import handlers.handler_commands;
import models_github.*;
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

        Logger.info("** incomming webhook! **");
        JsonNode json = request().body().asJson();

        final String sender_name = json.get("sender").get("login").asText();
        if (sender_name.equals("theindiepocalypse")) {
            // we don't want to be responding to ourselves
            return ok();
        }

        // Extracting the hook

        interface_github_webhook hook;
        if (model_webhook_issue_comment_created.is_me(json)) {
            hook = model_webhook_issue_comment_created.from_json(json);
        }
        else if (model_webhook_pull_request_comment_created.is_me(json)) {
            hook = model_webhook_pull_request_comment_created.from_json(json);
        }
        else if (model_webhook_issue_created.is_me(json)) {
            hook = model_webhook_issue_created.from_json(json);
        }
        else if (model_webhook_pull_request_created_or_updated.is_me(json)) {
            model_webhook_pull_request_created_or_updated tmp_hook =
                model_webhook_pull_request_created_or_updated.from_json(json);
            store_local_db.update_pull_request(tmp_hook.pull_request);
            hook = tmp_hook;
        }
        else {
            Logger.info("we got some weird hook, not handled yet");
            return ok();
        }
        store_local_db.update_hook_components(hook);
        
        // running the commands

        ArrayList<String> command_responses = handler_commands.handle_from_hook(hook);

        // assembling response

        String response = hook.get_response()+"\n\n";
        for (String command_response: command_responses) {
            response += command_response+"\n\n";
        }

        if (response.trim().length()==0) {
            return ok();
        }
        response = "@"+sender_name+" "+response;
        if (!store_github_api.comment_on_issue(hook.get_repo(), hook.get_issue_num(), response)) {
            Logger.info("problem commenting...");
        };
        return ok();
    }
}

