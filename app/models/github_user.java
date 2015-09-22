package models;

/**
 * Created by skariel on 22/09/15.
 */

import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class github_user {
    public String login;
    @Id
    public int id;
    public String avatar_url;
    public String gravatar_id;
    public String url;
    public String html_url;
    public String followers_url;
    public String following_url;
    public String gists_url;
    public String starred_url;
    public String subscriptions_url;
    public String organizations_url;
    public String repos_url;
    public String events_url;
    public String received_events_url;
    public String type;
    public boolean site_admin;
    public String name;
    public String company;
    public String blog;
    public String location;
    public String email;
    public boolean hireable;
    public String bio;
    public int public_repos;
    public int public_gists;
    public int followers;
    public int following;
    public Formats.DateTime created_at;
    public Formats.DateTime updated_at;
}
