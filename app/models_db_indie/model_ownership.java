package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.CacheStrategy;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_ownership extends Model {
    private static final Finder<String, model_ownership> find = new Finder<>(model_ownership.class);

    @Id
    @Nonnull
    public final String id;
    @Nonnull
    public final String user_name;
    @Nonnull
    public final String repo_name;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal percent;
    private final boolean is_creator;

    public model_ownership(
            @Nonnull String p_user_name,
            @Nonnull String p_repo_name,
            @Nonnull BigDecimal p_percent,
            @Nonnull Boolean p_is_creator) {
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

    public static model_ownership with_new_percent(
            @Nonnull model_ownership base_ownership,
            @Nonnull BigDecimal new_percent) {
        assert base_ownership != null;
        assert new_percent != null;
        return new model_ownership(
                base_ownership.user_name,
                base_ownership.repo_name,
                new_percent,
                base_ownership.is_creator
        );
    }

    public static model_ownership from_sqlrow(@Nonnull SqlRow row) {
        assert row != null;
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

