package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import handlers.handler_commands;
import models_commands.model_command_issue_comment;
import models_commands.model_command_issue_created;
import models_commands.model_command_pull_request_comment;
import models_commands.model_command_pull_request_created_or_updated;
import models_github.model_webhook_issue_comment_created;
import models_github.model_webhook_issue_created;
import models_github.model_webhook_pull_request_comment_created;
import models_github.model_webhook_pull_request_created_or_updated;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by skariel on 12/10/15.
 */
public class controller_webhooks_github extends Controller {
    public Result handle_wildcard() {
        // TODO: remove excessive logging.info and add datetime to log entries
        // TODO: consider using the github api package for java. See here: http://github-api.kohsuke.org/
        // TODO: factor common stuff into the handler_commands

        Logger.info("** incomming webhook! **");

        JsonNode json = request().body().asJson();
        //Logger.info(json.toString());


        if (model_webhook_issue_comment_created.is_me(json)) {
            Logger.info("we have a new comment on some issue! parsing and sending response!");
            model_webhook_issue_comment_created hook = model_webhook_issue_comment_created.from_json(json);
            if (!hook.user.user_name.equals("theindiepocalypse")) {
                // we don't want to respond to ourselves in a recursive manner, right? ;)
                model_command_issue_comment command = new model_command_issue_comment(hook.repo, hook.issue);
                handler_commands.handle_command(command);
            }
            return ok();
        }
        if (model_webhook_pull_request_comment_created.is_me(json)) {
            Logger.info("we have a new comment on some pull_request! parsing and sending response!");
            model_webhook_pull_request_comment_created hook = model_webhook_pull_request_comment_created.from_json(json);
            if (!hook.user.user_name.equals("theindiepocalypse")) {
                // we don't want to respond to ourselves in a recursive manner, right? ;)
                model_command_pull_request_comment command = new model_command_pull_request_comment(hook.repo, hook.issue);
                handler_commands.handle_command(command);
            }
            return ok();
        }
        if (model_webhook_issue_created.is_me(json)) {
            Logger.info("we have a new issue! parsing and sending response!");
            model_webhook_issue_created hook = model_webhook_issue_created.from_json(json);
            if (!hook.user.user_name.equals("theindiepocalypse")) {
                // we don't want to respond to ourselves in a recursive manner, right? ;)
                model_command_issue_created command = new model_command_issue_created(hook.repo, hook.issue);
                handler_commands.handle_command(command);
            }
            return ok();
        }
        if (model_webhook_pull_request_created_or_updated.is_me(json)) {
            Logger.info("we have a new pull_request! parsing and sending response!");
            model_webhook_pull_request_created_or_updated hook = model_webhook_pull_request_created_or_updated.from_json(json);
            if (!hook.user.user_name.equals("theindiepocalypse")) {
                // we don't want to respond to ourselves in a recursive manner, right? ;)
                model_command_pull_request_created_or_updated command = new model_command_pull_request_created_or_updated(hook.repo, hook.pull_request);
                handler_commands.handle_command(command);
            }
            return ok();
        }



//        if (model_webhook_issue_comment_created.is_me(json)) {
//            Logger.info("we have a new comment on some issue! parsing and sending response!");
//            model_webhook_issue_comment_created hook = model_webhook_issue_comment_created.from_json(json);
//
//            if (!hook.user.user_name.equals("theindiepocalypse")) {
//                // we don't want to respond to ourselves in a recursive manner, right? ;)
//                if (hook.comment.body.contains("@theindiepocalypse create readme")) {
//                    store_github_iojs.create_readme(hook.repo, "I did this!");
//                    store_github_api.comment_on_issue(hook.repo, hook.issue, "@"+hook.user.user_name+
//                            " done, I created a README!");
//                    return ok();
//                }
//
//                store_github_api.comment_on_issue(hook.repo, hook.issue, "i'm on it!");
//            }
//        }
//
//        if (model_webhook_pull_request_comment_created.is_me(json)) {
//            Logger.info("we have a new comment on some pull request! parsing and sending response!");
//            model_webhook_pull_request_comment_created hook = model_webhook_pull_request_comment_created.from_json(json);
//
//            if (!hook.user.user_name.equals("theindiepocalypse")) {
//                // we don't want to respond to ourselves in a recursive manner, right? ;)
//                if (hook.comment.body.contains("@theindiepocalypse create readme")) {
//                    store_github_iojs.create_readme(hook.repo, "I did this!");
//                    store_github_api.comment_on_issue(hook.repo, hook.issue, "@"+hook.user.user_name+
//                            " done, I created a README!");
//                    return ok();
//                }
//
//                store_github_api.comment_on_issue(hook.repo, hook.issue, "thanks for this pull request!");
//            }
//        }

        return ok();
    }
}

