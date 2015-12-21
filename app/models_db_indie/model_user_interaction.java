package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_user_interaction extends Model {
    private static final Finder<String, model_user_interaction> find = new Finder<>(model_user_interaction.class);

    @ManyToOne
    public final model_user user;
    @Id
    private final String id;
    final Date date_performed;
    final boolean is_hook;
    final boolean is_image_upload;
    final boolean is_new_repo_created;
    final boolean is_login;
    final String action_type;
    final String action_sub_type;
    final String action_description;
    final String p1;
    final String p1_desc;
    final String p2;
    final String p2_desc;
    final String p3;
    final String p3_desc;
    final String p4;
    final String p4_desc;
    final String p5;
    final String p5_desc;

    private model_user_interaction(
            final model_user p_user,
            final boolean p_is_hook,
            final boolean p_is_image_upload,
            final boolean p_is_new_repo_created,
            final boolean p_is_login,
            final String p_action_type,
            final String p_action_sub_type,
            final String p_action_description,
            final String p_p1,
            final String p_p1_desc,
            final String p_p2,
            final String p_p2_desc,
            final String p_p3,
            final String p_p3_desc,
            final String p_p4,
            final String p_p4_desc
    ) {
        this.date_performed = new Date();
        this.id = p_user.user_name + "@action@" + p_action_type + " @ " + date_performed.toString();
        this.user = p_user;
        this.is_hook = p_is_hook;
        this.is_image_upload = p_is_image_upload;
        this.is_new_repo_created = p_is_new_repo_created;
        this.is_login = p_is_login;
        this.action_type = p_action_type;
        this.action_sub_type = p_action_sub_type;
        this.action_description = p_action_description;
        this.xxxxxxxxxxxxx

    }

    public static Query<model_user_interaction> fetch() {
        return find.setUseQueryCache(true).fetch("user");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

