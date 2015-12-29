package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import utils.utils_github_webhooks;

/**
 * Created by skariel on 14/10/15.
 */
public class model_webhook_pull_request_created_or_updated implements interface_github_webhook {
    private final enum_webhook_action action;
    private final String number;
    private final model_pull_request pull_request;
    private final model_repo repo;
    private final model_user user;

    private model_webhook_pull_request_created_or_updated(
            String p_action,
            String p_number,
            model_pull_request p_pull_request,
            model_repo p_repo,
            model_user p_user
    ) {
        this.action = utils_github_webhooks.from_string(p_action);
        this.number = p_number;
        this.pull_request = p_pull_request;
        this.repo = p_repo;
        this.user = p_user;
    }

    public static model_webhook_pull_request_created_or_updated from_json(JsonNode json) {
        String action = json.get("action").asText();
        String number = Integer.toString(json.get("number").asInt());
        model_pull_request pull_request = model_pull_request.from_webhook_json(json.get("pull_request"));
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
                json.has("sender") && json.size() == 5;
    }

    private boolean is_update() {
        return this.action.equals(enum_webhook_action.SYNCHRONIZE);
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
        return pull_request;
    }

    @Override
    public String get_issue_num() {
        return number;
    }

    @Override
    public String get_comment() {
        if (is_update()) {
            return "";
        }
        return pull_request.body;
    }

    @Override
    public model_issue get_issue() {
        return null;
    }

    @Override
    public enum_webhook_action get_action() {
        return action;
    }


}
