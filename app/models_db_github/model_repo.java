package models_db_github;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheStrategy;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by name")
@Entity
public class model_repo extends Model {
    static final Finder<String, model_repo> find = new Finder<>(model_repo.class);

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
        String name = json_repo.get("name").asText();
        String description = json_repo.get("description").asText();
        String github_html_url = json_repo.get("html_url").asText();
        String homepage = json_repo.get("homepage").asText();
        Integer stars_count = json_repo.get("stargazers_count").asInt();
        Integer forks_count = json_repo.get("forks_count").asInt();
        return new model_repo(name, description, homepage, github_html_url, stars_count, forks_count);
    }

    public static model_repo from_name_desc_and_homepage(String name, String desc, String homepage) {
        return new model_repo(name, desc, homepage, "https://github.com/theindiepocalypse/" + name, 0, 0);
    }

    public static Finder<String, model_repo> fetch() {
        return find;
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

