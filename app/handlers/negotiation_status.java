package handlers;

import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_db_indie.*;
import play.Logger;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by skariel on 23/11/15.
 */
class negotiation_status {

    public class transaction_info_mem {
        public model_merge_transaction transaction;
        public model_offer_for_merge offer;
        public model_ownership from_user_ownership;
        public transaction_info_mem(
                model_merge_transaction p_transaction,
                model_offer_for_merge p_offer,
                model_ownership p_from_user_ownership
        ) {
            this.transaction = p_transaction;
            this.offer = p_offer;
            this.from_user_ownership = p_from_user_ownership;
        }
    }

    public final List<transaction_info_mem> implied_transactions_mem;
    public final model_request_for_merge request;
    public final model_ownership to_user_ownership;

    private final BigDecimal ownership_currently_accepted;
    private final List<String> users_name_currently_accepted;
    private final BigDecimal required_ownership_as_per_policy;
    private final BigDecimal requested_percent;
    private final BigDecimal required_for_acceptance_current_best_case;
    private final List<String /* user name */> users_name_with_more_ownership;
    private final BigDecimal total_ownership_of_users_with_more_ownership;

    // we assume here to_user has ownership. This is taken care of in the hook checkin
    public negotiation_status(model_pull_request p_pull_request,
                              model_request_for_merge p_request,
                              List<model_offer_for_merge> offers,
                              model_repo_policy policy,
                              List<model_ownership> ownerships,
                              model_repo p_repo) {

        request = p_request;

        users_name_currently_accepted = new ArrayList<>(5);
        final Map<String /* user_name */, model_ownership> ownership_from_user_name = new HashMap<>(11);
        users_name_with_more_ownership = new ArrayList<>();
        BigDecimal tmp_total_ownership_of_users_with_more_ownership = new BigDecimal("0.0");
        model_ownership to_ownership = null;
        if (ownerships != null) {
            for (model_ownership ownership : ownerships) {
                ownership_from_user_name.put(ownership.user_name, ownership);
            }
            if (ownership_from_user_name.containsKey(p_pull_request.user_name)) {
                to_ownership = ownership_from_user_name.get(p_pull_request.user_name);
            } else {
                Logger.error("to_user \"" + p_pull_request.user_name + "\" has no ownership in pull request #" + p_pull_request.number + " for repo \"" + p_repo.repo_name + "\", this should not happen!");
            }
            for (model_ownership ownership : ownerships) {
                if (ownership.percent.compareTo(to_ownership.percent) > 0) {
                    users_name_with_more_ownership.add(ownership.user_name);
                    tmp_total_ownership_of_users_with_more_ownership = tmp_total_ownership_of_users_with_more_ownership.add(ownership.percent);
                }
            }
        }
        total_ownership_of_users_with_more_ownership = tmp_total_ownership_of_users_with_more_ownership;

        if (offers != null) {
            // sort offers in descending order!
            Collections.sort(offers, (o1, o2) -> -o1.amount_percent.compareTo(o2.amount_percent));
        }

        if (policy != null) {
            required_ownership_as_per_policy = policy.ownership_required_to_merge_pull_requests;
        } else {
            required_ownership_as_per_policy = null;
        }
        if (p_request != null) {
            requested_percent = p_request.amount_percent;
        } else {
            requested_percent = null;
        }

        if ((offers != null) && (p_request != null)) {
            BigDecimal tmp_ownership_currently_accepted = new BigDecimal("0.0");
            for (model_offer_for_merge offer : offers) {
                if (offer.amount_percent.compareTo(p_request.amount_percent) >= 0) {
                    users_name_currently_accepted.add(offer.user_name);
                    tmp_ownership_currently_accepted = tmp_ownership_currently_accepted
                            .add(ownership_from_user_name.get(offer.user_name).percent);
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
                tmp_ownership = tmp_ownership.add(ownership_from_user_name.get(offer.user_name).percent);
                if (tmp_ownership.compareTo(policy.ownership_required_to_merge_pull_requests) >= 0) {
                    tmp_best_offer = offer.amount_percent;
                    break;
                }
            }
            required_for_acceptance_current_best_case = tmp_best_offer;
        } else {
            required_for_acceptance_current_best_case = null;
        }

        implied_transactions_mem = new ArrayList<>();

        // generating transactions!

        if ((is_negotiation_succesful()) && (total_ownership_of_users_with_more_ownership.compareTo(BigDecimal.ZERO) > 0)) {

            Map<String /* user name */, model_offer_for_merge> offer_from_user_name = new HashMap<>(11);
            for (model_offer_for_merge offer : offers) {
                offer_from_user_name.put(offer.user_name, offer);
            }

            assert p_request != null;
            BigDecimal transaction_quanta = p_request.amount_percent.divide(total_ownership_of_users_with_more_ownership);
            for (String user_name : users_name_with_more_ownership) {

                final model_ownership ownership = ownership_from_user_name.get(user_name);
                final BigDecimal user_ownership = ownership.percent;
                final BigDecimal transaction_amount_for_user = transaction_quanta.multiply(user_ownership);

                final String p_from_user_name = user_name;
                final String p_to_user_name = p_request.user_name;
                final model_offer_for_merge p_offer = offer_from_user_name.get(user_name);
                final BigDecimal p_amount_percent = transaction_amount_for_user;
                final Date p_date = new Date();
                final model_ownership p_from_user_ownership = ownership;
                final model_ownership p_to_user_ownership = to_ownership;

                final String p_offer_id = p_offer==null? null : p_offer.id;

                model_merge_transaction merge_transaction = new model_merge_transaction(
                        p_from_user_name,
                        p_to_user_name,
                        p_pull_request.id,
                        p_offer_id,          // p_offer is nullable!
                        p_request.id,
                        p_amount_percent,
                        p_date,
                        p_from_user_ownership.id,
                        p_to_user_ownership.id,
                        p_repo.repo_name,
                        p_from_user_ownership.percent);

                implied_transactions_mem.add(new transaction_info_mem(merge_transaction, p_offer, p_from_user_ownership));

            }
        }
        this.to_user_ownership = to_ownership;
    }

    public String toString() {
        // TODO: turn this into a nicely formatted table
        // TODO: list accepted and unaccepted offers and ownership per user, etc.
        // TODO: better wording

        String res = "";

        res += "ownership currently accepted: ";
        if (ownership_currently_accepted != null) {
            res += ownership_currently_accepted.toString() + "%";
        } else {
            res += "0%";
        }
        res += "\n";

        res += "users currently accepted: ";
        if ((users_name_currently_accepted != null) && (users_name_currently_accepted.size() > 0)) {
            int i = 0;
            for (String user_name : users_name_currently_accepted) {
                i += 1;
                res += "@" + user_name;
                if (i < users_name_currently_accepted.size()) {
                    res += ", ";
                }
            }
        } else {
            res += "none";
        }
        res += "\n";

        res += "required ownership for merge, per policy: ";
        if (required_ownership_as_per_policy != null) {
            res += required_ownership_as_per_policy.toString() + "%";
        } else {
            res += "(null? please report problem to admins!)";
            // TODO: elaborate here, maybe repo name?
            Logger.error("null required ownership to merge as per policy!");
        }
        res += "\n";

        res += "requested ownership to merge: ";
        if (requested_percent != null) {
            res += requested_percent.toString() + "%";
        } else {
            res += "no request was made yet";
        }
        res += "\n";

        res += "required for acceptance: ";
        if (required_for_acceptance_current_best_case != null) {
            res += required_for_acceptance_current_best_case.toString() + "%";
        } else {
            res += "not even for free ;)";
        }
        res += "\n";

        res += "is negotiation succesful? ";
        if (is_negotiation_succesful()) {
            res += "yes!";
        } else {
            res += "not yet";
        }
        res += "\n";

        return res;
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
