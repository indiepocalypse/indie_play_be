package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_pull_request;
import models.model_repo;
import models.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_webhook_pull_request_created_or_updated {
    public final String action;
    public final Integer number;
    public final model_pull_request pull_request;
    public final model_repo repo;
    public final model_user user;
    // TODO: add created and updated dates, labels, milestone, etc.
    // TODO: use the utils class for the json parsing

    public model_webhook_pull_request_created_or_updated(
            String p_action,
            Integer p_number,
            model_pull_request p_pull_request,
            model_repo p_repo,
            model_user p_user
    ) {
        this.action = p_action;
        this.number = p_number;
        this.pull_request = p_pull_request;
        this.repo = p_repo;
        this.user = p_user;
    }

    public boolean is_update() {
        return this.action.equals("synchronize");
    }

    public static model_webhook_pull_request_created_or_updated from_json(JsonNode json) {
        String action = json.get("action").asText();
        Integer number = json.get("number").asInt();
        model_pull_request pull_request = model_pull_request.from_json(json.get("pull_request"));
        model_repo repo = model_repo.from_json(json.get("repository"));
        model_user user = model_user.from_json(json.get("sender"));
        return new model_webhook_pull_request_created_or_updated(
                action,
                number,
                pull_request,
                repo,
                user
        );
    }

    public static boolean is_me(JsonNode json) {
        return json.has("action") &&
                ((json.get("action").asText().equals("opened")) || (json.get("action").asText().equals("synchronize"))) &&
                json.has("number") && json.has("pull_request") && json.has("repository") &&
                json.has("sender") && json.size()==5;
    }
}
