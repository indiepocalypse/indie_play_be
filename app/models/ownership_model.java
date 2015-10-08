package models;

import com.avaje.ebean.Model;

import javax.persistence.*;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class ownership_model extends Model {
    @Id
    public String id;

    @OneToOne
    @JoinColumn(name="user_name")
    public user_model user;
    @OneToOne
    @JoinColumn(name="repo_name")
    public repo_model repo;

    public double percent;

    public ownership_model(user_model p_user, repo_model p_repo, double p_percent) {
        id = p_user.user_name+"@"+p_repo.repo_name;
        user = p_user;
        repo = p_repo;
        percent = p_percent;
    }
    public static Finder<String, ownership_model> find = new Finder<String, ownership_model>(ownership_model.class);
}

