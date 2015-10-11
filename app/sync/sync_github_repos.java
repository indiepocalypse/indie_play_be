package sync;

import models.model_repo;
import play.Logger;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;

import java.util.List;
import java.util.Random;

/**
 * Created by skariel on 06/10/15.
 */
public class sync_github_repos {
    static Thread t1 = null;
    static boolean syncing = false;

    static public void start() {
        if (t1 == null) {
            t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            sync();
                            Thread.sleep(store_conf.get_github_repo_sync_delta_milis()+
                                    store_conf.get_github_repo_sync_minimum_milis());
                            Random rand = new Random();
                            int jitter = (int)(rand.nextFloat()*stores.store_conf.get_github_repo_sync_jitter_milis());
                            Thread.sleep(jitter);
                        } catch (Exception e) {
                            Logger.error("while sleeping to sync with github...", e);
                        }
                    }
                }
            };
            t1.start();
        }
    }

    static public void stop() {
        if (t1 != null) {
            t1.interrupt();
            t1 = null;
        }
    }

    static void sync() {
        if (syncing) {
            return;
        }
        syncing = true;

        List<model_repo> repos = store_github_api.get_indie_repositories();
        for (model_repo repo : repos) {
            try {
                Thread.sleep(stores.store_conf.get_github_repo_sync_jitter_small_milis());
            }
            catch (Exception ignored) {
            }
            store_local_db.update_repo(repo);
        }
        Logger.info("syncing " + Integer.toString(repos.size())+" github repos");

        syncing = false;
    }
}
