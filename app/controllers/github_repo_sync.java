package controllers;

import models.model_repo;
import play.Logger;
import stores.store_local_db;
import stores.store_github_api;

import java.util.List;

/**
 * Created by skariel on 06/10/15.
 */
public class github_repo_sync {
    static Thread t1 = null;
    static boolean syncing = false;

    static public void start() {
        if (t1 == null) {
            t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            sync();
                            Thread.sleep(store_local_db.get_github_repo_sync_delta_milis());
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
        for (model_repo repo: repos) {
            store_local_db.update_repo(repo);
        }
        Logger.info("SYNCSYNC SIZE=" + Integer.toString(repos.size()));

        syncing = false;
    }
}
