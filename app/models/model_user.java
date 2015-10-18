package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.annotation.EntityConcurrencyMode;
import com.fasterxml.jackson.databind.JsonNode;
import utils.utils_json;

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
        String user_avatar_url = utils_json.str_or_null(json_user, "avatar_url");
        String user_name = utils_json.str_or_null(json_user, "login");
        String user_blog_url = utils_json.str_or_null(json_user, "blog");
        String user_github_html_url = utils_json.str_or_null(json_user, "html_url");
        String user_mail = utils_json.str_or_null(json_user, "email");
        Integer user_public_repos = utils_json.int_or_null(json_user, "public_repos");
        String user_github_repos_url = utils_json.str_or_null(json_user, "repos_url");
        Integer followers = utils_json.int_or_null(json_user, "followers");
        Integer following = utils_json.int_or_null(json_user, "following");
        return new model_user(
                user_name, user_blog_url, user_github_html_url,
                user_mail, user_avatar_url, user_public_repos,
                user_github_repos_url, followers, following);
    }
}

