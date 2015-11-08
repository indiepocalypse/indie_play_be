package models_db_indie;

import com.avaje.ebean.Model;
import models_db_github.model_repo;
import models_db_github.model_user;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_ownership extends Model {
    public static final Finder<String, model_ownership> find = new Finder<>(model_ownership.class);
    @Id
    public final String id;
    @ManyToOne
    public final model_user user;
    @ManyToOne
    public final model_repo repo;
    public final BigDecimal percent;

    public model_ownership(model_user p_user, model_repo p_repo, BigDecimal p_percent) {
        id = p_user.user_name + "@" + p_repo.repo_name;
        user = p_user;
        repo = p_repo;
        percent = p_percent;
    }
}

