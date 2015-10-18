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
public class model_admins extends Model {
    public static final Finder<String, model_admins> find = new Finder<>(model_admins.class);
    @Id
    public final String id;
    @ManyToOne
    public final model_user user;

    public model_admins(model_user p_user) {
        id = p_user.user_name + "@admins";
        user = p_user;
    }
}

