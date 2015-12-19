package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_repo;
import stores.store_conf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Created by skariel on 29/09/15.
 */

@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_repo_policy extends Model {
    static final Finder<String, model_repo_policy> find = new Finder<>(model_repo_policy.class);

    @Id
    public final String id;
    @ManyToOne
    public final model_repo repo;
    @Column(precision = 5, scale = 2)
    public final BigDecimal ownership_required_to_change_policy;
    @Column(precision = 5, scale = 2)
    public final BigDecimal ownership_required_to_manage_issues; // close/label/etc.
    @Column(precision = 5, scale = 2)
    public final BigDecimal ownership_required_to_merge_pull_requests;

    public model_repo_policy(model_repo p_repo, BigDecimal change, BigDecimal manage, BigDecimal merge) {
        id = p_repo.repo_name + "@policy";
        this.repo = p_repo;
        this.ownership_required_to_change_policy = change;
        this.ownership_required_to_manage_issues = manage;
        this.ownership_required_to_merge_pull_requests = merge;
    }

    // constructor with some default values from global conf file:
    public model_repo_policy(model_repo p_repo) {
        this(p_repo,
                store_conf.get_policy_default_ownership_required_to_change_policy(),
                store_conf.get_policy_default_ownership_required_to_manage_issues(),
                store_conf.get_policy_default_ownership_required_to_merge_pull_request()
        );
    }

    public static Query<model_repo_policy> fetch() {
        return find.setUseQueryCache(true)
                .fetch("repo");
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

    public model_repo_policy same_but_with_different_change_manage_and_merge_policies(BigDecimal change, BigDecimal manage, BigDecimal merge) {
        return new model_repo_policy(
                this.repo,
                change,
                manage,
                merge
        );
    }

    public model_repo_policy same_but_with_different_policy_to_change_policy(BigDecimal change_policy) {
        return this.same_but_with_different_change_manage_and_merge_policies(
                change_policy,
                this.ownership_required_to_manage_issues,
                this.ownership_required_to_merge_pull_requests
        );
    }

    public model_repo_policy same_but_with_different_policy_to_manage_issues(BigDecimal issues_policy) {
        return this.same_but_with_different_change_manage_and_merge_policies(
                this.ownership_required_to_change_policy,
                issues_policy,
                this.ownership_required_to_merge_pull_requests
        );
    }

    public model_repo_policy same_but_with_different_policy_to_merge_pull_requests(BigDecimal merge_policy) {
        return this.same_but_with_different_change_manage_and_merge_policies(
                this.ownership_required_to_change_policy,
                this.ownership_required_to_manage_issues,
                merge_policy
        );
    }


}

