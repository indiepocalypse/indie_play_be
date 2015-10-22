package models_github;

import models.model_pull_request;
import models.model_repo;
import models.model_user;

/**
 * Created by skariel on 20/10/15.
 */
public interface interface_github_webhook {
    model_repo get_repo(); // always should exist
    model_user get_user(); // always should exist
    model_pull_request get_pull_request(); // this one can be null
    int get_issue_num();
    String get_comment();
    String get_response();

}
