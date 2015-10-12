package sync;

import models.model_user;
import play.Logger;
import stores.store_conf;
import stores.store_github_api;
import stores.store_local_db;

import java.util.List;
import java.util.Random;

/**
 * Created by skariel on 06/10/15.
 */
public class sync_github_users {
    private static Thread t1 = null;
    private static boolean syncing = false;

    static public void start() {
        if (t1 == null) {
            t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            sync();
                            Thread.sleep(store_conf.get_github_user_sync_delta_milis());
                            Random rand = new Random();
                            int jitter = (int)(rand.nextFloat()* store_conf.get_github_user_sync_jitter_milis()+
                                    store_conf.get_github_user_sync_minimum_milis());
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

    private static void sync() {
        if (syncing) {
            return;
        }
        syncing = true;

        List<model_user> users = store_local_db.get_all_users();
        Logger.info("syncing " + Integer.toString(users.size())+" users with github");
        for (model_user user: users) {
            try {
                Thread.sleep(store_conf.get_github_user_sync_jitter_small_milis());
            }
            catch (Exception ignored) {
            }
            user = store_github_api.get_user_by_name(user.user_name);
            store_local_db.update_user(user);
        }

        syncing = false;
    }
}
