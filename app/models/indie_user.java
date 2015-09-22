package models;

/**
 * Created by skariel on 22/09/15.
 */

import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class indie_user {
    @Id
    public String id;
    
    // TODO: should be like a supersetof the github user, include also shares etc.
    // this is just an initial list, most likely not complete at all.

    public String indie_repos_with_shares_url;
    public String indie_pull_requests_url;
    public String indie_home_html_url;
    public int indie_followers;
    public int indie_following;
    public String indie_followers_url;
    public String indie_following_url;
    public github_user github_user;
    public String indie_starred_url;
}
