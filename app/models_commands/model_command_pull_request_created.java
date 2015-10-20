package models_commands;

import models.model_repo;
import models.model_pull_request;

/**
 * Created by skariel on 18/10/15.
 */
public class model_command_pull_request_created {
    public final String comment_body;
    public final model_repo repo;
    public final model_pull_request pull_request;
    public model_command_pull_request_created(model_repo p_repo, model_pull_request p_pull_request) {
        this.repo = p_repo;
        this.pull_request = p_pull_request;
        this.comment_body = "Thanks for creating this pull request!";
    }
}