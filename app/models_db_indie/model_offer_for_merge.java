package models_db_indie;

import com.avaje.ebean.Model;
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

@Entity
public class model_offer_for_merge extends Model {
    public static final Finder<String, model_offer_for_merge> find = new Finder<>(model_offer_for_merge.class);
    @Id
    public final String id;
    @ManyToOne
    public final model_user user;
    @ManyToOne
    public final model_pull_request pull_request;
    @Column(precision = 7, scale = 4)
    public final BigDecimal amount_percent;
    final Boolean is_active;
    final Boolean was_positively_accepted;
    final public Date date_accepted_if_accepted;
    final public Date date_created;

    public model_offer_for_merge(model_user p_user, model_pull_request p_pull_request, BigDecimal p_amount_percent, Boolean p_is_active, Boolean p_was_positively_accepted, Date p_date_created, Date p_date_accepted_if_accepted) {
        id = "offer_for_merge_from_user_"+p_user.user_name + "_for_pull_request_number_"+p_pull_request.number+"_for_repo_"+p_pull_request.repo.repo_name;
        this.user = p_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
        this.is_active = p_is_active;
        this.was_positively_accepted = p_was_positively_accepted;
        this.date_created = p_date_created;
        this.date_accepted_if_accepted = p_date_accepted_if_accepted;
    }
}
