package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_user extends Model {
    public static final Finder<String, model_user> find = new Finder<>(model_user.class);
    @Id
    public final String user_name;
    public final String user_blog_url;
    public final String github_html_url;
    public final String email;
    public final String avatar_url;
    public final Integer public_repos;
    public final String github_repos_url;
    public final Integer followers;
    public final Integer following;

    public model_user(String p_user_name, String p_user_blog_url, String p_github_html_url,
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

    public static model_user from_json(JsonNode json_user) {
        String user_avatar_url = json_user.get("avatar_url").asText();
        String user_name = json_user.get("login").asText();
        String user_blog_url = json_user.get("blog").asText();
        String user_github_html_url = json_user.get("html_url").asText();
        String user_mail = json_user.get("email").asText();
        Integer user_public_repos = json_user.get("public_repos").asInt();
        String user_github_repos_url = json_user.get("repos_url").asText();
        Integer followers = json_user.get("followers").asInt();
        Integer following = json_user.get("following").asInt();
        return new model_user(
                user_name, user_blog_url, user_github_html_url,
                user_mail, user_avatar_url, user_public_repos,
                user_github_repos_url, followers, following);
    }
}

