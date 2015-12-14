package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.annotation.EntityConcurrencyMode;
import models_db_github.model_repo;
import models_db_github.model_user;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_ownership extends Model {
    static final Finder<String, model_ownership> find = new Finder<>(model_ownership.class);

    @Id
    public final String id;
    @ManyToOne
    public final model_user user;
    @ManyToOne
    public final model_repo repo;
    @Column(precision = 5, scale = 2)
    public final BigDecimal percent;
    public final boolean is_creator;

    public model_ownership(model_user p_user, model_repo p_repo, BigDecimal p_percent, boolean p_is_creator) {
        id = "ownershio@" + p_user.user_name + "@" + p_repo.repo_name;
        user = p_user;
        repo = p_repo;
        percent = p_percent;
        is_creator = p_is_creator;
    }

    // TODO: this solution is no good in the sense that it's not recursive...
    public static Query<model_ownership> fetch() {
        return find
                .fetch("user")
                .fetch("repo");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }
}

