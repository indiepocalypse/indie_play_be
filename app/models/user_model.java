package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class user_model extends Model {
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
        this.github_repos_url =p_github_repos_url;
        this.followers = p_followers;
        this.following = p_following;
    }
    public static Finder<String, user_model> find = new Finder<String, user_model>(user_model.class);
}

