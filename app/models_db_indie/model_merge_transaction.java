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
public class model_merge_transaction extends Model {
    public static final Finder<String, model_merge_transaction> find = new Finder<>(model_merge_transaction.class);
    @Id
    public final String id;
    @ManyToOne
    public final model_user from_user;
    @ManyToOne
    public final model_user to_user;
    @ManyToOne
    public final model_pull_request pull_request;
    @ManyToOne
    public final model_offer_for_merge offer;
    @ManyToOne
    public final model_request_for_merge request;
    @Column(precision = 5, scale = 2)
    public final BigDecimal amount_percent;
    final public Date date;

    public model_merge_transaction(model_user p_from_user, model_user p_to_user, model_pull_request p_pull_request, model_offer_for_merge p_offer, model_request_for_merge p_request, BigDecimal p_amount_percent, Date p_date) {
        id = "transaction_from_user_" + p_from_user.user_name + "_to_user "+p_to_user+"_for_pull_request_number_" + p_pull_request.number + "_for_repo_" + p_pull_request.repo.repo_name;
        this.to_user = p_to_user;
        this.from_user = p_from_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
        this.offer = p_offer;
        this.request = p_request;
        this.date = p_date;
    }
}

