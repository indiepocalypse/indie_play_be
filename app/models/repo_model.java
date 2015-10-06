package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class repo_model extends Model {
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
    public static Finder<String, repo_model> find = new Finder<String,repo_model>(repo_model.class);
}

