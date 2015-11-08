package models_memory_github;

import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;

/**
 * Created by skariel on 20/10/15.
 */
public interface interface_github_webhook {
    model_repo get_repo(); // always should exist

    model_user get_user(); // always should exist

    model_pull_request get_pull_request(); // this one can be null

    model_issue get_issue(); // this one can be null

    String get_issue_num();

    String get_comment();

    enum_webhook_action get_action();
}
