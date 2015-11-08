package models_db_indie;

import com.avaje.ebean.Model;
import models_db_github.model_repo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_repo_policy extends Model {
    // TODO: integrate in db store
    public static final Finder<String, model_repo_policy> find = new Finder<>(model_repo_policy.class);
    @Id
    public final String id;
    @ManyToOne
    public final model_repo repo;
    BigDecimal ownership_required_to_change_policy;
    BigDecimal ownership_required_to_manage_issues; // close/label/etc.

    public model_repo_policy(model_repo p_repo, BigDecimal p_ownership_required_to_change_policy, BigDecimal p_ownership_required_to_manage_issues) {
        id = p_repo.repo_name + "@policy";
        this.repo = p_repo;
        this.ownership_required_to_change_policy = p_ownership_required_to_change_policy;
        this.ownership_required_to_manage_issues = p_ownership_required_to_manage_issues;
    }
}

