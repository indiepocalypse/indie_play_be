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
public class model_user_extended_info extends Model {
    private static final Finder<String, model_user_extended_info> find = new Finder<>(model_user_extended_info.class);
    public final String user_name;
    @Id
    private final String id;
    public final boolean is_admin;
    public final boolean rate_limit_was_communicated_to_user_via_github_comment;

    private model_user_extended_info(final String p_user_name, final boolean p_is_admin, final boolean p_rate_limit_communicated_to_user_via_github_comment) {
        assert p_user_name != null;

        this.id = get_id_by_user_name(p_user_name);
        this.user_name = p_user_name;
        this.is_admin = p_is_admin;
        this.rate_limit_was_communicated_to_user_via_github_comment = p_rate_limit_communicated_to_user_via_github_comment;
    }

    public static String get_id_by_user_name(final String user_name) {
        return user_name + "@extended_info";
    }

    public static model_user_extended_info create(final String p_user_name, final boolean p_is_admin) {
        final boolean rate_limit_was_communicated_to_user_via_github_comment = false;
        return new model_user_extended_info(p_user_name, p_is_admin, rate_limit_was_communicated_to_user_via_github_comment);
    }

    public model_user_extended_info set_ratelimit_communicated_to_user_via_github_comment(final boolean newstatus) {
        return new model_user_extended_info(user_name, is_admin, newstatus);
    }

    public model_user_extended_info set_admin(final boolean newstatus) {
        return new model_user_extended_info(user_name, newstatus, rate_limit_was_communicated_to_user_via_github_comment);
    }

    public static Query<model_user_extended_info> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

