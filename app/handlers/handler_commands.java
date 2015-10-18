package handlers;

import models_commands.model_command_issue_comment;
import models_commands.model_command_issue_created;
import models_commands.model_command_pull_request_comment;
import models_commands.model_command_pull_request_created;
import stores.store_github_api;

/**
 * Created by skariel on 17/10/15.
 */
public class handler_commands {
    // TODO: implement!
    // this classs will handle commands usually sent through github comments

    public static void handle_command(model_command_issue_comment command) {
        store_github_api.comment_on_issue(command.repo, command.issue.number, command.comment_body);
    }

    public static void handle_command(model_command_pull_request_comment command) {
        store_github_api.comment_on_issue(command.repo, command.issue.number, command.comment_body);
    }

    public static void handle_command(model_command_issue_created command) {
        store_github_api.comment_on_issue(command.repo, command.issue.number, command.comment_body);
    }

    public static void handle_command(model_command_pull_request_created command) {
        store_github_api.comment_on_issue(command.repo, command.pull_request.number, command.comment_body);
    }
}
