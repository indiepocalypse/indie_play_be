package models;

import com.avaje.ebean.Model;

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
    BigDecimal amount_percent;

    public model_offer(model_user p_user, model_pull_request p_pull_request, BigDecimal p_amount_percent) {
        id = p_user.user_name + "@admins";
        this.user = p_user;
        this.pull_request = p_pull_request;
        this.amount_percent = p_amount_percent;
    }
}

