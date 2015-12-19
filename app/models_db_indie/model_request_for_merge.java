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
public class model_request_for_merge extends Model {
    static final Finder<String, model_request_for_merge> find = new Finder<>(model_request_for_merge.class);

    @Id
    public final String id;
    @ManyToOne
    public final model_user user;
    @ManyToOne
    public final model_pull_request pull_request;
    @Column(precision = 5, scale = 2)
    public final BigDecimal amount_percent;
    @Column(precision = 5, scale = 2)
    public final BigDecimal user_ownership_percent;
    public final Date date_accepted_if_accepted;
    public final Date date_created;
    final Boolean is_active;
    final Boolean was_positively_accepted;

    public model_request_for_merge(
            model_user p_user,
            model_pull_request p_pull_request,
            BigDecimal p_amount_percent,
            Boolean p_is_active,
            Boolean p_was_positively_accepted,
            Date p_date_created,
            Date p_date_accepted_if_accepted,
            BigDecimal p_user_ownership_percent) {
        id = "request_for_merge_from_user_" + p_user.user_name + "_for_pull_request_number_" + p_pull_request.number + "_for_repo_" + p_pull_request.repo.repo_name;
        this.user = p_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
        this.is_active = p_is_active;
        this.was_positively_accepted = p_was_positively_accepted;
        this.date_created = p_date_created;
        this.date_accepted_if_accepted = p_date_accepted_if_accepted;
        this.user_ownership_percent = p_user_ownership_percent;
    }

    public static model_request_for_merge same_but_accepted_now(model_request_for_merge model_request_for_merge) {
        if (model_request_for_merge == null) {
            return null;
        }
        boolean is_active = false;
        boolean was_positively_accepted = true;
        Date date_accepted_if_accepted = new Date();
        return new model_request_for_merge(
                model_request_for_merge.user,
                model_request_for_merge.pull_request,
                model_request_for_merge.amount_percent,
                is_active,
                was_positively_accepted,
                model_request_for_merge.date_created,
                date_accepted_if_accepted,
                model_request_for_merge.user_ownership_percent
        );
    }

    public static Query<model_request_for_merge> fetch() {
        return find.setUseQueryCache(true)
                .fetch("user")
                .fetch("pull_request");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }


}

