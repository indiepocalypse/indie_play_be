package handlers;

import models_db_indie.model_offer_for_merge;
import models_db_indie.model_repo_policy;
import models_db_indie.model_request_for_merge;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by skariel on 23/11/15.
 */
public class negotiation_status {
    public final BigDecimal ownership_currently_accepted;
    public final BigDecimal required_ownership_as_per_policy;
    public final BigDecimal requested_percent;
    public final BigDecimal required_requested_percent_for_acceptance_as_per_policy;

    public negotiation_status(model_request_for_merge request, List<model_offer_for_merge> offers, model_repo_policy policy) {
        if (policy!=null) {
            required_ownership_as_per_policy = policy.percentage_to_merge;
        }
        else {
            required_ownership_as_per_policy = null;
        }
        if (request != null) {
            requested_percent = request.amount_percent;
        }
        else {
            requested_percent = null;
        }

        if (offers != null) {
            BigDecimal tmp_ownership_currently_accepted = new BigDecimal("0.0");
            for (model_offer_for_merge offer: offers) {
                if xxx {
                    tmp_ownership_currently_accepted = tmp_ownership_currently_accepted.add(offer.user.);
                }
            }
            ownership_currently_accepted = tmp_ownership_currently_accepted;
        }
        else {
            ownership_currently_accepted = null;
            required_requested_percent_for_acceptance_as_per_policy = null;
        }
    }

    public boolean is_negotiation_succesful() {
        if ((ownership_currently_accepted==null) ||
            (required_ownership_as_per_policy==null) ||
            (requested_percent==null) ||
            (required_ownership_as_per_policy==null))
        {
            return false;
        }
        return ownership_currently_accepted.compareTo(required_ownership_as_per_policy) >= 0;
    }
}
