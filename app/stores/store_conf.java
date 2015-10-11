package stores;

import com.typesafe.config.ConfigFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 11/10/15.
 */
public class store_conf {
    public static long get_github_repo_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.github.repo.delta_milis", TimeUnit.MILLISECONDS);

    }

    public static long get_gmail_reload_sync_delta_milis() {
        return ConfigFactory.load().getDuration("sync.gmail.reload.delta_milis", TimeUnit.MILLISECONDS);
    }
}
