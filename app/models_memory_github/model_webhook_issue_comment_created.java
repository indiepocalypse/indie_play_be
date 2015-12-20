package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import utils.utils_github_webhooks;

/**
 * Created by skariel on 14/10/15.
 */
public class model_webhook_issue_comment_created implements interface_github_webhook {
    private final enum_webhook_action action;
    private final model_issue issue;
    private final model_comment comment;
    private final model_repo repo;
    private final model_user user;

    private model_webhook_issue_comment_created(
            String p_action,
            model_issue p_issue,
            model_comment p_comment,
            model_repo p_repo,
            model_user p_user
    ) {
        this.action = utils_github_webhooks.from_string(p_action);
        this.issue = p_issue;
        this.comment = p_comment;
        this.repo = p_repo;
        this.user = p_user;
    }

    public static model_webhook_issue_comment_created from_json(JsonNode json) {
        String action = json.get("action").asText();
        model_issue issue = model_issue.from_json(json.get("issue"));
        model_comment comment = model_comment.from_json(json.get("comment"));
        model_repo repo = model_repo.from_json(json.get("repository"));
        model_user user = model_user.from_json(json.get("sender"));
        return new model_webhook_issue_comment_created(
                action,
                issue,
                comment,
                repo,
                user
        );
    }

    public static boolean is_me(JsonNode json) {
        return json.has("action") && json.get("action").asText().equals("created") &&
                json.has("issue") && json.has("comment") &&
                json.has("repository") && json.has("sender") &&
                json.size() == 5 && (!json.get("issue").has("pull_request"));
    }

    @Override
    public model_repo get_repo() {
        return repo;
    }

    @Override
    public model_user get_user() {
        return user;
    }

    @Override
    public model_pull_request get_pull_request() {
        return null;
    }

    @Override
    public model_issue get_issue() {
        return issue;
    }

    @Override
    public String get_issue_num() {
        return issue.number;
    }

    @Override
    public String get_comment() {
        return comment.body;
    }

    @Override
    public enum_webhook_action get_action() {
        return action;
    }
}
