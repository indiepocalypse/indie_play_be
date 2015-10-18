package models_commands;

import models.model_repo;
import models_github.model_issue;

/**
 * Created by skariel on 18/10/15.
 */
public class model_command_issue_created {
    public final String comment_body;
    public final model_repo repo;
    public final model_issue issue;
    public model_command_issue_created(model_repo p_repo, model_issue p_issue) {
        this.repo = p_repo;
        this.issue = p_issue;
        this.comment_body = "Thanks for creating this issue!";
    }
}
