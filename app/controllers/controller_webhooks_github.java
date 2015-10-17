package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models_github.webhook_comment_created;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import stores.store_github_api;
import stores.store_github_iojs;

/**
 * Created by skariel on 12/10/15.
 */
public class controller_webhooks_github extends Controller {
    public Result handle_wildcard() {
        // TODO: implement ;)
        // TODO: give some app id to the webhooks
        // TODO: remember last webhook date, sync from last date on startup
        // TODO: remove excessive logging.info and add datetime to log entries
        // TODO: consider using the github api package for java. See here: http://github-api.kohsuke.org/
        // TODO: write a handler_commands

        Logger.info("** incomming webhook! **");

        JsonNode json =request().body().asJson();
        Logger.info(json.toString());

        if (webhook_comment_created.is_me(json)) {
            Logger.info("we have a new comment on some issue! parsing and sending response!");
            webhook_comment_created hook = webhook_comment_created.from_json(json);

            if (!hook.user.user_name.equals("theindiepocalypse")) {
                // we don't want to respond to ourselves in a recursive manner, right? ;)
                if (hook.comment.body.contains("@theindiepocalypse create readme")) {
                    store_github_iojs.create_readme(hook.repo, "I did this!");
                    store_github_api.comment_on_issue(hook.repo, hook.issue, "@"+hook.user.user_name+
                            " done, I created a README!");
                    return ok();
                }
                store_github_api.comment_on_issue(hook.repo, hook.issue, "i'm on it!");
            }
        }

        return ok();
    }
}

