package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_repo;
import models.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_webhook_issue_comment_created implements interface_github_webhook {
    public final String action;
    public final model_issue issue;
    public final model_comment comment;
    public final model_repo repo;
    public final model_user user;
    // TODO: add created and updated dates, labels, milestone, etc.

    public model_webhook_issue_comment_created(
            String p_action,
            model_issue p_issue,
            model_comment p_comment,
            model_repo p_repo,
            model_user p_user
    ) {
        this.action = p_action;
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
                json.size()==5 && (!json.get("issue").has("pull_request"));
    }

    @Override
    public model_repo get_repo() {
        return repo;
    }

    @Override
    public int get_issue_num() {
        return issue.number;
    }

    @Override
    public String get_comment() {
        return comment.body;
    }

    @Override
    public void handle_locally() {
        // ignore for now
    }

    @Override
    public String get_response() {
        return "Thanks for commenting on this issue. I'm on it!";
    }
}
