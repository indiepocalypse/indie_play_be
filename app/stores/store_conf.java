package stores;

import com.typesafe.config.ConfigFactory;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 11/10/15.
 */
public class store_conf {

    private static long __get_delay_with_jitter(long base, double jitter_frac) {
        Random rand = new Random();
        return (long) (Math.abs(rand.nextGaussian()) * base * jitter_frac + base);
    }

    public static long get_delay_L1_milis() {
        long base = ConfigFactory.load().getDuration("delay.L1", TimeUnit.MILLISECONDS);
        double jitter_frac = ConfigFactory.load().getDouble("delay.jitter.fraction");
        return __get_delay_with_jitter(base, jitter_frac);
    }

    public static double get_delay_L1_seconds() {
        return 0.001 * get_delay_L1_milis();
    }

    public static long get_delay_L2_milis() {
        long base = ConfigFactory.load().getDuration("delay.L2", TimeUnit.MILLISECONDS);
        double jitter_frac = ConfigFactory.load().getDouble("delay.jitter.fraction");
        return __get_delay_with_jitter(base, jitter_frac);
    }

    public static double get_delay_L2_seconds() {
        return 0.001 * get_delay_L2_milis();
    }

    public static long get_delay_L3_milis() {
        long base = ConfigFactory.load().getDuration("delay.L3", TimeUnit.MILLISECONDS);
        double jitter_frac = ConfigFactory.load().getDouble("delay.jitter.fraction");
        return __get_delay_with_jitter(base, jitter_frac);
    }

    public static double get_delay_L3_seconds() {
        return 0.001 * get_delay_L3_milis();
    }

    private static String get_url_heroku_root() {
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

    public static BigDecimal get_policy_floor_ownership_required_to_manage_issues() {
        return new BigDecimal(ConfigFactory.load().getString("policy.floor_ownership_required_to_manage_issues"));
    }

    public static BigDecimal get_policy_default_ownership_required_to_change_policy() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_ownership_required_to_change_policy"));
    }

    public static BigDecimal get_policy_floor_ownership_required_to_change_policy() {
        return new BigDecimal(ConfigFactory.load().getString("policy.floor_ownership_required_to_change_policy"));
    }

    public static BigDecimal get_policy_default_ownership_required_to_merge_pull_request() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_ownership_to_merge_pull_request_policy"));
    }

    public static BigDecimal get_policy_floor_ownership_required_to_merge_pull_request() {
        return new BigDecimal(ConfigFactory.load().getString("policy.floor_ownership_to_merge_pull_request_policy"));
    }

    public static BigDecimal get_policy_default_ownership_required_to_manage_repo() {
        return new BigDecimal(ConfigFactory.load().getString("policy.default_ownership_to_manage_repo"));
    }

    public static BigDecimal get_policy_floor_ownership_required_to_manage_repo() {
        return new BigDecimal(ConfigFactory.load().getString("policy.floor_ownership_to_manage_repo"));
    }

    public static boolean get_debug_should_send_mails() {
        return ConfigFactory.load().getBoolean("debug.should_send_mails");
    }

    public static boolean get_debug_should_check_mails() {
        return ConfigFactory.load().getBoolean("debug.should_check_mails");
    }

    public static int get_rate_limit_for_L2_delay() {
        return ConfigFactory.load().getInt("ratelimit.maximum_for_L2_delay");
    }

    public static String get_indie_mail_address() {
        return ConfigFactory.load().getString("indie.mail.address");
    }

    public static int get_rate_limit_maximum_commands_per_comment() {
        return ConfigFactory.load().getInt("ratelimit.maximum_commands_per_comment");
    }
}
