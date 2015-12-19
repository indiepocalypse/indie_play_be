package sync;

import handlers.handler_general;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import play.Logger;
import stores.github_io_exception;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;

import java.util.List;
import java.util.Random;

/**
 * Created by skariel on 06/10/15.
 */
public class sync_github_repos {
    private static Thread t1 = null;
    private static boolean interrupted = false;
    private static boolean initially_synced = false;

    static public void start() {
        // some defensive shit here :)
        stop();

        t1 = new Thread() {
            public void run() {
                while (!interrupted) {
                    try {
                        if ((!initially_synced) && (!interrupted)) {
                            sync();
                            initially_synced = true;
                        }
                        Random rand = new Random();
                        int jitter = (int) (store_conf.get_github_repo_sync_minimum_milis() +
                                rand.nextFloat() * stores.store_conf.get_github_repo_sync_jitter_milis());
                        if (!interrupted) {
                            Thread.sleep(store_conf.get_github_repo_sync_delta_milis() + jitter);
                        } else {
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

    private static void sync() {
        List<model_repo> repos;
        try {
            repos = store_github_api.get_indie_repositories();
        } catch (Exception e) {
            Logger.error("while retrieving repos from github during a repo sync", e);
            return;
        }
        Logger.info("syncing " + Integer.toString(repos.size()) + " github repos");
        for (model_repo repo : repos) {
            try {
                if (!interrupted) {
                    Thread.sleep(stores.store_conf.get_github_repo_sync_jitter_small_milis());
                } else {
                    return;
                }
            } catch (Exception e) {
                if (!interrupted) {
                    Logger.error("while syncing github repos", e);
                }
            }
            Logger.info(":: updating self repo " + repo.repo_name);
            if (interrupted) {
                return;
            }
            Logger.info(":: updating self repo " + repo.repo_name);
            try {
                store_local_db.update_repo(repo);
            } catch (Exception e) {
                Logger.error("could not sync repo " + repo.repo_name);
            }
            try {
                store_github_api.create_webhook(repo);
            } catch (github_io_exception ignore) {
            }
            final boolean check_first_for_existance = true;
            handler_general.create_default_readme(repo, check_first_for_existance);
            try {
                List<model_pull_request> all_pull_requests_for_repo = store_github_api.get_all_pull_requests(repo);
                all_pull_requests_for_repo.forEach(pull_request -> {
                    store_local_db.update_user(pull_request.user);
                    handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);
                });
            } catch (Exception e) {
                Logger.error("could not get pull requests from repo " + repo.repo_name + " while syncing");
            }
        }
        Logger.info("done syncing " + Integer.toString(repos.size()) + " github repos");
    }
}
