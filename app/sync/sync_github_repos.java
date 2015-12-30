package sync;

import handlers.handler_general;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import play.Logger;
import stores.github_io_exception;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;

import java.util.List;

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
                        if (!interrupted) {
                            Thread.sleep(store_conf.get_delay_L3_milis());
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

    private static void stop() {
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
                    Thread.sleep(stores.store_conf.get_delay_L1_milis());
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
                    try {
                        final model_user user = store_github_api.get_user_by_name(pull_request.user_name);
                        store_local_db.update_user(user);
                    } catch (github_io_exception e) {
                        Logger.error("could not update user named " + pull_request.user_name + " during updating repo named " + repo.repo_name);
                    }
                    handler_general.locally_update_pull_request_and_clear_offers_if_necessary(pull_request);
                });
            } catch (Exception e) {
                Logger.error("could not get pull requests from repo " + repo.repo_name + " while syncing");
            }
        }
        Logger.info("done syncing " + Integer.toString(repos.size()) + " github repos");
    }
}
