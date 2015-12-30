package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import play.Logger;
import play.api.libs.iteratee.Error;
import stores.store_local_db;
import utils.utils_github_webhooks;

import javax.annotation.Nonnull;

/**
 * Created by skariel on 14/10/15.
 */
public class model_webhook_pull_request_comment_created implements interface_github_webhook {
    @Nonnull
    private final enum_webhook_action action;
    @Nonnull
    private final model_issue issue;
    @Nonnull
    private final model_pull_request pull_request;
    @Nonnull
    private final model_comment comment;
    @Nonnull
    private final model_repo repo;
    @Nonnull
    private final model_user user;

    private model_webhook_pull_request_comment_created(
            @Nonnull String p_action,
            @Nonnull model_issue p_issue,
            @Nonnull model_comment p_comment,
            @Nonnull model_repo p_repo,
            @Nonnull model_user p_user
    ) throws Exception {
        assert p_action != null;
        assert p_issue != null;
        assert p_comment != null;
        assert p_repo != null;
        assert p_user != null;

        this.action = utils_github_webhooks.from_string(p_action);
        this.issue = p_issue;
        this.comment = p_comment;
        this.repo = p_repo;
        this.user = p_user;
        final model_pull_request tmp_pull_request = store_local_db.get_pull_request_by_repo_name_and_number(repo.repo_name, issue.number);
        if (tmp_pull_request==null) {
            // TODO: should we try to sync the repo from github?
            String message = "trying to create a comment on pull request but cannot get the PR from the DB. repo_name is "+repo.repo_name+" issue #"+p_issue.number;
            Logger.error(message);
            // TODO: make a better custom error, using the info that we have
            throw new Exception(message);
        }
        this.pull_request = tmp_pull_request;
    }

    public static model_webhook_pull_request_comment_created from_json(@Nonnull JsonNode json) throws Exception {
        assert json != null;
        String action = json.get("action").asText();
        model_issue issue = model_issue.from_json(json.get("issue"));
        model_comment comment = model_comment.from_json(json.get("comment"));
        model_repo repo = model_repo.from_json(json.get("repository"));
        model_user user = model_user.from_json(json.get("sender"));
        return new model_webhook_pull_request_comment_created(
                action,
                issue,
                comment,
                repo,
                user
        );
    }

    public static boolean is_me(@Nonnull JsonNode json) {
        assert json != null;
        return json.has("action") && json.get("action").asText().equals("created") &&
                json.has("issue") && json.has("comment") &&
                json.has("repository") && json.has("sender") &&
                json.size() == 5 && (json.get("issue").has("pull_request"));
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
        return issue.number;
    }

    @Override
    public String get_comment() {
        return comment.body;
    }

    @Override
    public model_issue get_issue() {
        return issue;
    }

    @Override
    public enum_webhook_action get_action() {
        return action;
    }


}
