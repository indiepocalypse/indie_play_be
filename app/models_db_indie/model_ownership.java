package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_repo;
import models_db_github.model_user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_ownership extends Model {
    private static final Finder<String, model_ownership> find = new Finder<>(model_ownership.class);

    @Id
    public final String id;
    public final String user_name;
    public final String repo_name;
    @Column(precision = 5, scale = 2)
    public final BigDecimal percent;
    private final boolean is_creator;

    public model_ownership(String p_user_name, String p_repo_name, BigDecimal p_percent, Boolean p_is_creator) {
        assert p_user_name != null;
        assert p_repo_name != null;
        assert p_percent != null;
        assert p_is_creator != null;

        this.id = "ownershio@" + p_user_name + "@" + p_repo_name;
        this.user_name = p_user_name;
        this.repo_name = p_repo_name;
        this.percent = p_percent;
        this.is_creator = p_is_creator;
    }

    public static model_ownership with_new_percent(model_ownership base_ownership, BigDecimal new_percent) {
        return new model_ownership(
                base_ownership.user_name,
                base_ownership.repo_name,
                new_percent,
                base_ownership.is_creator
        );
    }

    public static model_ownership from_sqlrow(SqlRow row) {
        return new model_ownership(
                row.getString("user_name"),
                row.getString("repo_name"),
                row.getBigDecimal("percent"),
                row.getBoolean("is_creator")
        );
    }

    // TODO: this solution is no good in the sense that it's not recursive...
    public static Query<model_ownership> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }
}

