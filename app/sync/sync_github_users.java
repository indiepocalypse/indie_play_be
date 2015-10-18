package sync;

import handlers.handler_general;
import models.model_admin;
import models.model_user;
import play.Logger;
import stores.store_conf;
import stores.store_credentials;
import stores.store_github_api;
import stores.store_local_db;

import java.util.List;
import java.util.Random;

/**
 * Created by skariel on 06/10/15.
 */
public class sync_github_users {
    private static Thread t1 = null;
    private static boolean interrupted = false;

    static public void start() {
        stop();

        // some hard coded data
        model_user indiepocalypse = handler_general.get_integrate_github_user_by_name(store_credentials.github.name);
        model_user skariel = handler_general.get_integrate_github_user_by_name("skariel");
        model_admin admin_indiepocalypse = new model_admin(indiepocalypse);
        model_admin admin_skariel = new model_admin(skariel);
        store_local_db.update_admin(admin_indiepocalypse);
        store_local_db.update_admin(admin_skariel);

        t1 = new Thread() {
            public void run() {
                while (!interrupted) {
                    try {
                        sync();
                        Random rand = new Random();
                        int jitter = (int)(store_conf.get_github_user_sync_minimum_milis() +
                                rand.nextFloat()* store_conf.get_github_user_sync_jitter_milis());
                        Thread.sleep(store_conf.get_github_user_sync_delta_milis() + jitter);
                        if (interrupted) {
                            return;
                        }
                    } catch (Exception e) {
                        if (!interrupted) {
                            Logger.error("while sleeping to sync users with github...", e);
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
        List<model_user> users = store_local_db.get_all_users();
        Logger.info("syncing " + Integer.toString(users.size())+" users with github");
        for (model_user user: users) {
            try {
                Thread.sleep(store_conf.get_github_user_sync_jitter_small_milis());
            }
            catch (Exception e) {
                if (!interrupted) {
                    Logger.error("while syncing user with github...", e);
                }
            }
            if (interrupted) {
                return;
            }
            try {
                user = store_github_api.get_user_by_name(user.user_name);
            }
            catch (Exception e) {
                Logger.error("while getting user info from github during user sync...", e);
            }
            store_local_db.update_user(user);
        }
    }
}
