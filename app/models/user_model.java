package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class user_model extends Model {
    public static Finder<String, user_model> find = new Finder<String, user_model>(user_model.class);
    @Id
    public String user_name;
    public String user_blog_url;
    public String github_html_url;
    public String email;
    public String avatar_url;
    public Integer public_repos;
    public String github_repos_url;
    public Integer followers;
    public Integer following;

    public user_model(String p_user_name, String p_user_blog_url, String p_github_html_url,
                      String p_email, String p_avatar_url, Integer p_public_repos,
                      String p_github_repos_url, Integer p_followers, Integer p_following) {
        this.user_name = p_user_name;
        this.user_blog_url = p_user_blog_url;
        this.github_html_url = p_github_html_url;
        this.email = p_email;
        this.avatar_url = p_avatar_url;
        this.public_repos = p_public_repos;
        this.github_repos_url = p_github_repos_url;
        this.followers = p_followers;
        this.following = p_following;
    }

    public static user_model from_json(JsonNode json_user) {
        String user_avatar_url = json_user.get("avatar_url").asText();
        String user_name = json_user.get("login").asText();
        String user_blog_url = json_user.get("blog").asText();
        String user_github_html_url = json_user.get("html_url").asText();
        String user_mail = json_user.get("email").asText();
        Integer user_public_repos = json_user.get("public_repos").asInt();
        String user_github_repos_url = json_user.get("repos_url").asText();
        Integer followers = json_user.get("followers").asInt();
        Integer following = json_user.get("following").asInt();
        return new user_model(
                user_name, user_blog_url, user_github_html_url,
                user_mail, user_avatar_url, user_public_repos,
                user_github_repos_url, followers, following);
    }
}

