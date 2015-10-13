package stores;

import com.typesafe.config.ConfigFactory;
import controllers.routes;
import play.Routes;

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
        return get_url_heroku_root()+ controllers.routes.controller_webhooks_github.handle_wildcard();
    }
}
