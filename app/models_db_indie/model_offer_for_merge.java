package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_pull_request;
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
public class model_offer_for_merge extends Model {
    private static final Finder<String, model_offer_for_merge> find = new Finder<>(model_offer_for_merge.class);

    @Id
    public final String id;
    @ManyToOne
    public final model_user user;
    @Column(precision = 5, scale = 2)
    public final BigDecimal amount_percent;
    @Column(precision = 5, scale = 2)
    public final BigDecimal user_ownership_percent;
    final public Date date_accepted_if_accepted;
    final public Date date_created;
    @ManyToOne
    private final model_pull_request pull_request;
    private Boolean is_active;
    private Boolean was_positively_accepted;

    public model_offer_for_merge(
            model_user p_user,
            model_pull_request p_pull_request,
            BigDecimal p_amount_percent,
            Boolean p_is_active,
            Boolean p_was_positively_accepted,
            Date p_date_created,
            Date p_date_accepted_if_accepted,
            BigDecimal p_user_ownership_percent) {
        id = "offer_for_merge_from_user_" + p_user.user_name + "_for_pull_request_number_" + p_pull_request.number + "_for_repo_" + p_pull_request.repo.repo_name;
        this.user = p_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
        this.is_active = p_is_active;
        this.was_positively_accepted = p_was_positively_accepted;
        this.date_created = p_date_created;
        this.date_accepted_if_accepted = p_date_accepted_if_accepted;
        this.user_ownership_percent = p_user_ownership_percent;
    }

    public static model_offer_for_merge same_but_accepted_now(model_offer_for_merge model_offer_for_merge) {
        if (model_offer_for_merge == null) {
            return null;
        }
        final boolean is_active = false;
        final boolean was_positively_accepted = true;
        final Date date_accepted_if_accepted = new Date();
        return new model_offer_for_merge(
                model_offer_for_merge.user,
                model_offer_for_merge.pull_request,
                model_offer_for_merge.amount_percent,
                is_active,
                was_positively_accepted,
                model_offer_for_merge.date_created,
                date_accepted_if_accepted,
                model_offer_for_merge.user_ownership_percent
        );
    }

    public static Query<model_offer_for_merge> fetch() {
        return find.setUseQueryCache(true)
                .fetch("user")
                .fetch("pull_request");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }


}

