package models_github;

import models.model_repo;

/**
 * Created by skariel on 20/10/15.
 */
public interface interface_hook {
    model_repo get_repo();
    int get_issue_num();
    String get_comment();
    String get_response();
}
