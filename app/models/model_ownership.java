package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_ownership extends Model {
    public static Finder<String, model_ownership> find = new Finder<String, model_ownership>(model_ownership.class);
    @Id
    public String id;
    @ManyToOne
    public model_user user;
    @ManyToOne
    public model_repo repo;
    public BigDecimal percent;

    public model_ownership(model_user p_user, model_repo p_repo, BigDecimal p_percent) {
        id = p_user.user_name + "@" + p_repo.repo_name;
        user = p_user;
        repo = p_repo;
        percent = p_percent;
    }
}

