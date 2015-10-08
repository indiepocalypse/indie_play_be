package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class ownership_model extends Model {
    public static Finder<String, ownership_model> find = new Finder<String, ownership_model>(ownership_model.class);
    @Id
    public String id;
    @ManyToOne
    public user_model user;
    @ManyToOne
    public repo_model repo;
    public BigDecimal percent;

    public ownership_model(user_model p_user, repo_model p_repo, BigDecimal p_percent) {
        id = p_user.user_name + "@" + p_repo.repo_name;
        user = p_user;
        repo = p_repo;
        percent = p_percent;
    }
}

