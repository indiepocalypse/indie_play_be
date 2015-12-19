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
@CacheStrategy(readOnly = true, warmingQuery = "order by name")
@Entity
public class model_admin extends Model {
    static final Finder<String, model_admin> find = new Finder<>(model_admin.class);

    @Id
    public final String id;
    @ManyToOne
    public final model_user user;

    public model_admin(model_user p_user) {
        id = p_user.user_name + "@admins";
        user = p_user;
    }

    public static Query<model_admin> fetch() {
        return find.fetch("user");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

