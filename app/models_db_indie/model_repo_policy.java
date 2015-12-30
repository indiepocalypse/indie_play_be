package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_repo;
import stores.store_conf;

import javax.annotation.Nonnull;
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
    private static final Finder<String, model_repo_policy> find = new Finder<>(model_repo_policy.class);

    @Id
    @Nonnull
    public final String id;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal ownership_required_to_change_policy;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal ownership_required_to_manage_issues; // close/label/etc.
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal ownership_required_to_merge_pull_requests;
    @Column(precision = 5, scale = 2)
    @Nonnull
    public final BigDecimal ownership_required_to_manage_repo; // upload images, etc.
    @Nonnull
    private final String repo_name;

    private model_repo_policy(
            @Nonnull String p_repo_name,
            @Nonnull BigDecimal change,
            @Nonnull BigDecimal manage_issue,
            @Nonnull BigDecimal merge,
            @Nonnull BigDecimal manage_repo) {
        assert p_repo_name != null;
        assert change != null;
        assert manage_issue != null;
        assert merge != null;
        assert manage_repo != null;

        this.id = p_repo_name + "@policy";
        this.repo_name = p_repo_name;
        this.ownership_required_to_change_policy = change;
        this.ownership_required_to_manage_issues = manage_issue;
        this.ownership_required_to_merge_pull_requests = merge;
        this.ownership_required_to_manage_repo = manage_repo;
    }

    // constructor with some default values from global conf file:
    public model_repo_policy(@Nonnull model_repo p_repo) {
        this(p_repo.repo_name,
                store_conf.get_policy_default_ownership_required_to_change_policy(),
                store_conf.get_policy_default_ownership_required_to_manage_issues(),
                store_conf.get_policy_default_ownership_required_to_merge_pull_request(),
                store_conf.get_policy_default_ownership_required_to_manage_repo()
        );
    }

    public static Query<model_repo_policy> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }

    private model_repo_policy same_but_with_different_change_manage_and_merge_policies(
            @Nonnull BigDecimal change,
            @Nonnull BigDecimal manage_issue,
            @Nonnull BigDecimal merge,
            @Nonnull BigDecimal manage_repo) {
        assert change != null;
        assert manage_issue != null;
        assert merge != null;
        assert manage_repo != null;
        return new model_repo_policy(
                this.repo_name,
                change,
                manage_issue,
                merge,
                manage_repo
        );
    }

    public model_repo_policy same_but_with_different_policy_to_change_policy(@Nonnull BigDecimal change_policy) {
        assert change_policy != null;
        return this.same_but_with_different_change_manage_and_merge_policies(
                change_policy,
                this.ownership_required_to_manage_issues,
                this.ownership_required_to_merge_pull_requests,
                this.ownership_required_to_manage_repo
        );
    }

    public model_repo_policy same_but_with_different_policy_to_manage_issues(@Nonnull BigDecimal issues_policy) {
        assert issues_policy != null;
        return this.same_but_with_different_change_manage_and_merge_policies(
                this.ownership_required_to_change_policy,
                issues_policy,
                this.ownership_required_to_merge_pull_requests,
                this.ownership_required_to_manage_repo
        );
    }

    public model_repo_policy same_but_with_different_policy_to_merge_pull_requests(@Nonnull BigDecimal merge_policy) {
        assert merge_policy != null;
        return this.same_but_with_different_change_manage_and_merge_policies(
                this.ownership_required_to_change_policy,
                this.ownership_required_to_manage_issues,
                merge_policy,
                this.ownership_required_to_manage_repo
        );
    }

    public model_repo_policy same_but_with_different_policy_to_manage_repo(@Nonnull BigDecimal manage_repo_policy) {
        assert manage_repo_policy != null;
        return this.same_but_with_different_change_manage_and_merge_policies(
                this.ownership_required_to_change_policy,
                this.ownership_required_to_manage_issues,
                this.ownership_required_to_merge_pull_requests,
                manage_repo_policy
        );
    }


}

