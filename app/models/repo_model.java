package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class repo_model extends Model {
    public static Finder<String, repo_model> find = new Finder<String, repo_model>(repo_model.class);
    @Id
    public String repo_name;
    public String repo_description;
    public String repo_homepage;
    public String github_html_url;
    public Integer stars_count;
    public Integer forks_count;

    public repo_model(String repo_name, String repo_description, String repo_homepage, String github_html_url, Integer stars_count, Integer forks_count) {
        this.repo_name = repo_name;
        this.repo_description = repo_description;
        this.repo_homepage = repo_homepage;
        this.github_html_url = github_html_url;
        this.stars_count = stars_count;
        this.forks_count = forks_count;
    }

    public static repo_model from_json(JsonNode json_repo) {
        String name = json_repo.get("name").asText("");
        String description = json_repo.get("description").asText("");
        String github_html_url = json_repo.get("html_url").asText("");
        String homepage = json_repo.get("homepage").asText("");
        Integer stars_count = json_repo.get("stargazers_count").asInt(0);
        Integer forks_count = json_repo.get("forks_count").asInt(0);
        return new repo_model(name, description, homepage, github_html_url, stars_count, forks_count);
    }

    public static repo_model from_name_desc_and_homepage(String name, String desc, String homepage) {
        return new repo_model(name, desc, homepage, "https://github.com/theindiepocalypse/" + name, 0, 0);
    }
}

