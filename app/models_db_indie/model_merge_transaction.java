package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_merge_transaction extends Model {
    private static final Finder<String, model_merge_transaction> find = new Finder<>(model_merge_transaction.class);
    public final String offer_id;
    public final String request_id;
    public final String from_user_ownership_id;
    public final String to_user_ownership_id;
    @Column(precision = 5, scale = 2)
    public final BigDecimal amount_percent;
    @Id
    private final String id;
    private final String from_user_name;
    private final String to_user_name;
    private final String pull_request_id;
    private final String repo_name;
    // the field below is needed since offer can be null
    // request on the other hand cannot be null, it already contains this data
    @Column(precision = 5, scale = 2)
    private final BigDecimal from_user_ownership_percent;
    private final Date date;

    public model_merge_transaction(
            String p_from_user_name,
            String p_to_user_name,
            String p_pull_request_id,
            String p_offer_id,
            String p_request_id,
            BigDecimal p_amount_percent,
            Date p_date,
            String p_from_user_ownership_id,
            String p_to_user_ownership_id,
            String p_repo_name,
            BigDecimal p_from_user_ownership_percent) {
        id = "transaction_from_user_" + p_from_user_name + "_to_user " + p_to_user_name + "_for_pull_request_id_" + p_pull_request_id + "_for_repo_" + p_repo_name;
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

    public static void deleteById(String id) {
        find.deleteById(id);
    }


}

