package stores;

import com.typesafe.config.ConfigFactory;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 11/10/15.
 */
public class store_conf {
    public static long get_github_repo_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.github.repo.delta_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_repo_sync_jitter_milis() {
        return ConfigFactory.load().getDuration("sync.github.repo.jitter_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_repo_sync_jitter_small_milis() {
        return ConfigFactory.load().getDuration("sync.github.repo.jitter_small_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_repo_sync_minimum_milis() {
        return ConfigFactory.load().getDuration("sync.github.repo.minimum_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_user_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.github.user.delta_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_user_sync_jitter_milis() {
        return ConfigFactory.load().getDuration("sync.github.user.jitter_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_user_sync_jitter_small_milis() {
        return ConfigFactory.load().getDuration("sync.github.user.jitter_small_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_github_user_sync_minimum_milis() {
        return ConfigFactory.load().getDuration("sync.github.user.minimum_milis", TimeUnit.MILLISECONDS);
    }

    public static long get_gmail_reload_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.gmail.reload.delta_milis", TimeUnit.MILLISECONDS);
    }

    public static long get_gmail_reload_sync_jitter_milis() {
        return ConfigFactory.load().getDuration("sync.gmail.reload.jitter_milis", TimeUnit.MILLISECONDS);
    }

    public static long get_gmail_reload_sync_jitter_small_milis() {
        return ConfigFactory.load().getDuration("sync.gmail.reload.jitter_small_milis", TimeUnit.MILLISECONDS);
    }

    public static long get_gmail_reload_sync_minimum_milis() {
        return ConfigFactory.load().getDuration("sync.gmail.reload.minimum_milis", TimeUnit.MILLISECONDS);
    }

    public static String get_url_heroku_root() {
        return ConfigFactory.load().getString("url.heroku.root");
    }

    public static String get_github_webhook_url() {
        return get_absolute_url(controllers.routes.controller_webhooks_github.handle_wildcard().url());
    }

    public static String get_absolute_url(String path) {
        return get_url_heroku_root() + path;
    }

    public static int get_policy_maximum_number_of_repos_per_user() {
        return ConfigFactory.load().getInt("policy.maximum_number_of_repos_per_user");
    }

    public static BigDecimal get_default_indie_ownership_percent() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_indie_ownership_percent"));
    }

    public static BigDecimal get_policy_default_ownership_required_to_manage_issues() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_ownership_required_to_manage_issues"));
    }

    public static BigDecimal get_policy_default_ownership_required_to_change_policy() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_ownership_required_to_change_policy"));
    }

    public static BigDecimal get_policy_floor_ownership_required_to_change_policy() {
        return new BigDecimal(ConfigFactory.load().getString("policy.floor_ownership_required_to_change_policy"));
    }

    public static BigDecimal get_policy_default_percentage_to_merge_policy() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_percentage_to_merge_policy"));
    }

    public static BigDecimal get_policy_floor_percentage_to_merge_policy() {
        return new BigDecimal(ConfigFactory.load().getString("policy.floor_percentage_to_merge_policy"));
    }

}
