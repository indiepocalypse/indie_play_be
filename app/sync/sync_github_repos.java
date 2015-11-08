package sync;

import handlers.handler_general;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import play.Logger;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by skariel on 06/10/15.
 */
public class sync_github_repos {
    private static Thread t1 = null;
    private static boolean interrupted = false;

    static public void start() {
        // some defensive shit here :)
        stop();

        t1 = new Thread() {
            public void run() {
                boolean first_time = true;
                while (!interrupted) {
                    try {
                        sync(first_time);
                        first_time = false;
                        Random rand = new Random();
                        int jitter = (int)(store_conf.get_github_repo_sync_minimum_milis() +
                                rand.nextFloat()*stores.store_conf.get_github_repo_sync_jitter_milis());
                        Thread.sleep(store_conf.get_github_repo_sync_delta_milis() + jitter);
                        if (interrupted) {
                            return;
                        }
                    } catch (Exception e) {
                        if (!interrupted) {
                            Logger.error("while sleeping to sync repos with github...", e);
                        }
                    }
                }
            }
        };

        interrupted = false;
        t1.start();
    }

    static public void stop() {
        interrupted = true;
        if (t1 != null) {
            t1.interrupt();
            t1 = null;
        }
    }

    private static void sync(boolean first_time) {
        List<model_repo> repos = new ArrayList<>();
        try {
            repos = store_github_api.get_indie_repositories();
        }
        catch (Exception e) {
            Logger.error("while retrieving repos from github during a repo sync", e);
        }
        for (model_repo repo : repos) {
            try {
                Thread.sleep(stores.store_conf.get_github_repo_sync_jitter_small_milis());
            }
            catch (Exception e) {
                if (!interrupted) {
                    Logger.error("while syncing github repos", e);
                }
            }
            Logger.info(":: updating self repo "+repo.repo_name);
            if (interrupted) {
                return;
            }
            Logger.info(":: updating self repo "+repo.repo_name);
            store_local_db.update_repo(repo);
            if (first_time) {
                store_github_api.create_webhook(repo);
                handler_general.create_default_readme_if_not_existing(repo);
                List<model_pull_request> all_pull_requests_for_repo = store_github_api.get_all_pull_requests(repo);
                for (model_pull_request pr: all_pull_requests_for_repo) {
                    store_local_db.update_pull_request(pr);
                }
            }
        }
        Logger.info("syncing " + Integer.toString(repos.size())+" github repos");
    }
}
