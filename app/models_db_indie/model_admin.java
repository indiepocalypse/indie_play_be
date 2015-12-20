package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_admin extends Model {
    private static final Finder<String, model_admin> find = new Finder<>(model_admin.class);
    @ManyToOne
    public final model_user user;
    @Id
    private final String id;

    public model_admin(model_user p_user) {
        id = p_user.user_name + "@admins";
        user = p_user;
    }

    public static Query<model_admin> fetch() {
        return find.setUseQueryCache(true).fetch("user");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

