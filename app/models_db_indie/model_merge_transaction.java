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
    @ManyToOne
    public final model_offer_for_merge offer;
    @ManyToOne
    public final model_request_for_merge request;
    @ManyToOne
    public final model_ownership from_user_ownership;
    @ManyToOne
    public final model_ownership to_user_ownership;
    @Column(precision = 5, scale = 2)
    public final BigDecimal amount_percent;
    @Id
    private final String id;
    @ManyToOne
    private final model_user from_user;
    @ManyToOne
    private final model_user to_user;
    @ManyToOne
    private final model_pull_request pull_request;
    @ManyToOne
    private final model_repo repo;
    // the field below is needed since offer can be null
    // request on the other hand cannot be null, it already contains this data
    @Column(precision = 5, scale = 2)
    private final BigDecimal from_user_ownership_percent;
    private final Date date;

    public model_merge_transaction(
            model_user p_from_user, model_user p_to_user,
            model_pull_request p_pull_request,
            model_offer_for_merge p_offer,
            model_request_for_merge p_request,
            BigDecimal p_amount_percent, Date p_date,
            model_ownership p_from_user_ownership,
            model_ownership p_to_user_ownership, model_repo p_repo,
            BigDecimal p_from_user_ownership_percent) {
        id = "transaction_from_user_" + p_from_user.user_name + "_to_user " + p_to_user + "_for_pull_request_number_" + p_pull_request.number + "_for_repo_" + p_pull_request.repo_name;
        this.to_user = p_to_user;
        this.from_user = p_from_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
        this.offer = p_offer;
        this.request = p_request;
        this.date = p_date;
        this.from_user_ownership = p_from_user_ownership;
        this.to_user_ownership = p_to_user_ownership;
        this.repo = p_repo;
        this.from_user_ownership_percent = p_from_user_ownership_percent;
    }

    public static Query<model_merge_transaction> fetch() {
        return find.setUseQueryCache(true)
                .fetch("from_user")
                .fetch("to_user")
                .fetch("pull_request")
                .fetch("offer")
                .fetch("request")
                .fetch("from_user_ownership")
                .fetch("to_user_ownership")
                .fetch("repo");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }


}

