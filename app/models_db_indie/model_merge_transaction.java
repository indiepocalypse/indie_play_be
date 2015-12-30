package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class model_merge_transaction extends Model {
    private static final Finder<String, model_merge_transaction> find = new Finder<>(model_merge_transaction.class);
    @Nullable
    public final String offer_id; // can be null since even if user didn't make an offer he is involved in the transaction
    @Nonnull
    public final String request_id;
    @Nonnull
    public final String from_user_ownership_id;
    @Nonnull
    public final String to_user_ownership_id;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal amount_percent;
    @Id
    @Nonnull
    private final String id;
    @Nonnull
    private final String from_user_name;
    @Nonnull
    private final String to_user_name;
    @Nonnull
    private final String pull_request_id;
    @Nonnull
    private final String repo_name;
    // the field below is needed since offer can be null
    // request on the other hand cannot be null, it already contains this data
    @Column(precision = 5, scale = 2)
    @Nonnull
    private final BigDecimal from_user_ownership_percent;
    @Nonnull
    private final Date date;

    public model_merge_transaction(
            @Nonnull String p_from_user_name,
            @Nonnull String p_to_user_name,
            @Nonnull String p_pull_request_id,
            @Nullable String p_offer_id,
            @Nonnull String p_request_id,
            @Nonnull BigDecimal p_amount_percent,
            @Nonnull Date p_date,
            @Nonnull String p_from_user_ownership_id,
            @Nonnull String p_to_user_ownership_id,
            @Nonnull String p_repo_name,
            @Nonnull BigDecimal p_from_user_ownership_percent) {
        assert p_to_user_name != null;
        assert p_from_user_name != null;
        assert p_pull_request_id != null;
        assert p_amount_percent != null;
        assert p_request_id != null;
        assert p_date != null;
        assert p_from_user_ownership_id != null;
        assert p_to_user_ownership_id != null;
        assert p_repo_name != null;
        assert p_from_user_ownership_percent != null;

        this.id = "transaction_from_user_" + p_from_user_name + "_to_user " + p_to_user_name + "_for_pull_request_id_" + p_pull_request_id + "_for_repo_" + p_repo_name;
        this.to_user_name = p_to_user_name;
        this.from_user_name = p_from_user_name;
        this.pull_request_id = p_pull_request_id;
        this.amount_percent = p_amount_percent;
        this.offer_id = p_offer_id;
        this.request_id = p_request_id;
        this.date = p_date;
        this.from_user_ownership_id = p_from_user_ownership_id;
        this.to_user_ownership_id = p_to_user_ownership_id;
        this.repo_name = p_repo_name;
        this.from_user_ownership_percent = p_from_user_ownership_percent;
    }

    public static Query<model_merge_transaction> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }


}

