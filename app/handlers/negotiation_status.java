package handlers;

import models_db_github.model_user;
import models_db_indie.model_offer_for_merge;
import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import models_db_indie.model_request_for_merge;
import play.Logger;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by skariel on 23/11/15.
 */
public class negotiation_status {
    public final BigDecimal ownership_currently_accepted;
    public final List<model_user> users_currently_accepted;
    public final BigDecimal required_ownership_as_per_policy;
    public final BigDecimal requested_percent;
    public final BigDecimal required_for_acceptance_current_best_case;

    public String toString() {
        // TODO: turn this into a nicely formatted table
        // TODO: list accepted and unaccepted offers and ownership per user, etc.
        // TODO: better wording

        String res = "";

        res += "ownership currently accepted: ";
        if (ownership_currently_accepted!=null) {
            res += ownership_currently_accepted.toString() + "%";
        }
        else {
            res += "0%";
        }
        res += "\n";

        res += "users currently accepted: ";
        if ((users_currently_accepted!=null)&&(users_currently_accepted.size()>0)) {
            int i=0;
            for (model_user user: users_currently_accepted) {
                i += 1;
                res += "@"+user.user_name;
                if (i<users_currently_accepted.size()) {
                    res += ", ";
                }
            }
        }
        else {
            res += "none";
        }
        res += "\n";

        res += "required ownership for merge, per policy: ";
        if (required_ownership_as_per_policy!=null) {
            res += required_ownership_as_per_policy.toString() + "%";
        }
        else {
            res += "(null? please report problem to admins!)";
            // TODO: elaborate here, maybe repo name?
            Logger.error("null required ownership to merge as per policy!");
        }
        res += "\n";

        res += "requested ownership to merge: ";
        if (requested_percent!=null) {
            res += requested_percent.toString() + "%";
        }
        else {
            res += "no request was made yet";
        }
        res += "\n";

        res += "required for acceptance: ";
        if (required_for_acceptance_current_best_case!=null) {
            res += required_for_acceptance_current_best_case.toString() + "%";
        }
        else {
            res += "not even for free ;)";
        }
        res += "\n";

        res += "is negotiation succesful? ";
        if (is_negotiation_succesful()) {
            res += "yes!";
        }
        else {
            res += "not yet";
        }
        res += "\n";

        return res;
    }

    public negotiation_status(model_request_for_merge request,
                              List<model_offer_for_merge> offers,
                              model_repo_policy policy,
                              List<model_ownership> ownerships) {

        users_currently_accepted = new ArrayList<>(5);

        Map<model_user, model_ownership> ownership_from_user = new HashMap<>(11);
        if (ownerships != null) {
            for (model_ownership ownership : ownerships) {
                ownership_from_user.put(ownership.user, ownership);
            }
        }

        if (offers != null) {
            // sort offers in descending order!
            Collections.sort(offers, (o1, o2) -> -o1.amount_percent.compareTo(o2.amount_percent));
        }

        if (policy != null) {
            required_ownership_as_per_policy = policy.ownership_required_to_merge_pull_requests;
        } else {
            required_ownership_as_per_policy = null;
        }
        if (request != null) {
            requested_percent = request.amount_percent;
        } else {
            requested_percent = null;
        }

        if ((offers != null) && (request != null)) {
            BigDecimal tmp_ownership_currently_accepted = new BigDecimal("0.0");
            for (model_offer_for_merge offer : offers) {
                if (offer.amount_percent.compareTo(request.amount_percent) >= 0) {
                    users_currently_accepted.add(offer.user);
                    tmp_ownership_currently_accepted = tmp_ownership_currently_accepted
                            .add(ownership_from_user.get(offer.user).percent);
                }
            }
            ownership_currently_accepted = tmp_ownership_currently_accepted;
        } else {
            ownership_currently_accepted = new BigDecimal("0.0");
        }


        if ((offers != null) && (policy != null)) {
            BigDecimal tmp_ownership = new BigDecimal("0.0");
            BigDecimal tmp_best_offer = null;
            for (model_offer_for_merge offer : offers) {
                tmp_ownership = tmp_ownership.add(ownership_from_user.get(offer.user).percent);
                if (tmp_ownership.compareTo(policy.ownership_required_to_merge_pull_requests) >= 0) {
                    tmp_best_offer = offer.amount_percent;
                    break;
                }
            }
            required_for_acceptance_current_best_case = tmp_best_offer;
        } else {
            required_for_acceptance_current_best_case = null;
        }
    }

    public boolean is_negotiation_succesful() {
        if ((ownership_currently_accepted == null) ||
                (required_ownership_as_per_policy == null) ||
                (requested_percent == null) ||
                (required_ownership_as_per_policy == null)) {
            return false;
        }
        return ownership_currently_accepted.compareTo(required_ownership_as_per_policy) >= 0;
    }
}
