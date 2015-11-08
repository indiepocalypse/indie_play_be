package models_db_indie;

import com.avaje.ebean.Model;
import models_db_github.model_pull_request;
import models_db_github.model_user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_offer extends Model {
    public static final Finder<String, model_offer> find = new Finder<>(model_offer.class);
    @Id
    public final String id;
    @ManyToOne
    public final model_user user;
    @ManyToOne
    public final model_pull_request pull_request;
    final BigDecimal amount_percent;
    final Boolean is_active;
    final Boolean was_positively_accepted;
    final Boolean date_accepted_if_accepted;
    final Boolean date_created;

    public model_offer(model_user p_user, model_pull_request p_pull_request, BigDecimal p_amount_percent, Boolean p_is_active, Boolean p_was_positively_accepted, Boolean p_date_created, Boolean p_date_accepted_if_accepted) {
        id = p_user.user_name + "@admins";
        this.user = p_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
        this.is_active = p_is_active;
        this.was_positively_accepted = p_was_positively_accepted;
        this.date_created = p_date_created;
        this.date_accepted_if_accepted = p_date_accepted_if_accepted;
    }
}

