package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_repo;
import models.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_webhook_issue_created {
    public String action;
    public model_issue issue;
    public model_repo repo;
    public model_user user;
    // TODO: add created and updated dates, labels, milestone, etc.

    public model_webhook_issue_created(
            String p_action,
            model_issue p_issue,
            model_repo p_repo,
            model_user p_user
    ) {
        this.action = p_action;
        this.issue = p_issue;
        this.repo = p_repo;
        this.user = p_user;
    }

    public static model_webhook_issue_created from_json(JsonNode json) {
        String action = json.get("action").asText();
        model_issue issue = model_issue.from_json(json.get("issue"));
        model_repo repo = model_repo.from_json(json.get("repository"));
        model_user user = model_user.from_json(json.get("sender"));
        return new model_webhook_issue_created(
                action,
                issue,
                repo,
                user
        );
    }

    public static boolean is_me(JsonNode json) {
        return json.has("action") && json.get("action").asText().equals("opened") &&
                json.has("issue") && json.has("repository") && json.has("sender") &&
                json.size()==4 && (!json.get("issue").has("pull_request"));
    }
}
