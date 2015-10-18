package models_commands;

import models.model_repo;
import models_github.model_issue;

/**
 * Created by skariel on 18/10/15.
 */
public class model_command_pull_request_created {
    public String comment_body;
    public model_repo repo;
    public model_issue issue;
    public model_command_pull_request_created(model_repo p_repo, model_issue p_issue) {
        this.repo = p_repo;
        this.issue = p_issue;
        this.comment_body = "Thanks for commenting on this pull request!";
    }
}
