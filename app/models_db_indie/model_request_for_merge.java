package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */

@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_request_for_merge extends Model {
    private static final Finder<String, model_request_for_merge> find = new Finder<>(model_request_for_merge.class);

    @Id
    @Nonnull
    public final String id;
    @Nonnull
    public final String user_name;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal amount_percent;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal user_ownership_percent;
    @Nonnull
    public final Date date_accepted_if_accepted;
    @Nonnull
    public final Date date_created;
    @Nonnull
    private final String pull_request_id;
    @Nonnull
    private final Boolean is_active;
    @Nonnull
    private final Boolean was_positively_accepted;

    public model_request_for_merge(
            @Nonnull String p_user_name,
            @Nonnull String p_pull_request_id,
            @Nonnull BigDecimal p_amount_percent,
            @Nonnull Boolean p_is_active,
            @Nonnull Boolean p_was_positively_accepted,
            @Nonnull Date p_date_created,
            @Nonnull Date p_date_accepted_if_accepted,
            @Nonnull BigDecimal p_user_ownership_percent) {
        assert p_user_name != null;
        assert p_pull_request_id != null;
        assert p_amount_percent != null;
        assert p_is_active != null;
        assert p_was_positively_accepted != null;
        assert p_date_created != null;
        assert p_date_accepted_if_accepted != null;
        assert p_user_ownership_percent != null;

        id = "request_for_merge_from_user_" + p_user_name + "_for_pull_request_id_" + p_pull_request_id;
        this.user_name = p_user_name;
        this.pull_request_id = p_pull_request_id;
        this.amount_percent = p_amount_percent;
        this.is_active = p_is_active;
        this.was_positively_accepted = p_was_positively_accepted;
        this.date_created = p_date_created;
        this.date_accepted_if_accepted = p_date_accepted_if_accepted;
        this.user_ownership_percent = p_user_ownership_percent;
    }

    public static model_request_for_merge same_but_accepted_now(@Nonnull model_request_for_merge model_request_for_merge) {
        assert model_request_for_merge != null;
        if (model_request_for_merge == null) {
            return null;
        }
        final boolean is_active = false;
        final boolean was_positively_accepted = true;
        final Date date_accepted_if_accepted = new Date();
        return new model_request_for_merge(
                model_request_for_merge.user_name,
                model_request_for_merge.pull_request_id,
                model_request_for_merge.amount_percent,
                is_active,
                was_positively_accepted,
                model_request_for_merge.date_created,
                date_accepted_if_accepted,
                model_request_for_merge.user_ownership_percent
        );
    }

    public static Query<model_request_for_merge> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }


}

