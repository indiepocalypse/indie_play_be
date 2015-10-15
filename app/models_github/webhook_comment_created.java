package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_repo;
import models.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class webhook_comment_created {
    public String action;
    public model_issue issue;
    public model_comment comment;
    public model_repo repo;
    public model_user user;
    // TODO: add created and updated dates, labels, milestone, etc.

    public webhook_comment_created(
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

    public static webhook_comment_created from_json(JsonNode json) {
        String action = json.get("action").asText();
        model_issue issue = model_issue.from_json(json.get("issue"));
        model_comment comment = model_comment.from_json(json.get("comment"));
        model_repo repo = model_repo.from_json(json.get("repository"));
        model_user user = model_user.from_json(json.get("sender"));
        return new webhook_comment_created(
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
}
