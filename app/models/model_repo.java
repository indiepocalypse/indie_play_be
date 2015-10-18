package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.annotation.EntityConcurrencyMode;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_repo extends Model {
    public static final Finder<String, model_repo> find = new Finder<>(model_repo.class);
    @Id
    public final String repo_name;
    public final String repo_description;
    public final String repo_homepage;
    public final String github_html_url;
    public final Integer stars_count;
    public final Integer forks_count;

    public model_repo(String repo_name, String repo_description, String repo_homepage, String github_html_url, Integer stars_count, Integer forks_count) {
        this.repo_name = repo_name;
        this.repo_description = repo_description;
        this.repo_homepage = repo_homepage;
        this.github_html_url = github_html_url;
        this.stars_count = stars_count;
        this.forks_count = forks_count;
    }

    public static model_repo from_json(JsonNode json_repo) {
        String name = utils.utils_json.str_or_null(json_repo, "name");
        String description = utils.utils_json.str_or_null(json_repo, "description");
        String github_html_url = utils.utils_json.str_or_null(json_repo, "html_url");
        String homepage = utils.utils_json.str_or_null(json_repo, "homepage");
        Integer stars_count = utils.utils_json.int_or_null(json_repo, "stargazers_count");
        Integer forks_count = utils.utils_json.int_or_null(json_repo, "forks_count");
        return new model_repo(name, description, homepage, github_html_url, stars_count, forks_count);
    }

    public static model_repo from_name_desc_and_homepage(String name, String desc, String homepage) {
        return new model_repo(name, desc, homepage, "https://github.com/theindiepocalypse/" + name, 0, 0);
    }
}

