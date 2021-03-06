package sync;

import handlers.handler_general;
import models_db_github.model_user;
import models_db_indie.model_user_extended_info;
import play.Logger;
import stores.store_conf;
import stores.store_credentials;
import stores.store_github_api;
import stores.store_local_db;

import java.util.List;

/**
 * Created by skariel on 06/10/15.
 */
public class sync_github_users {
    private static Thread t1 = null;
    private static boolean interrupted = false;
    private static boolean initially_synced = false;

    static public void start() {
        stop();

        // some hard coded data
        model_user indiepocalypse = handler_general.get_integrate_github_user_by_name(store_credentials.get_github_indie_user_name());
        model_user skariel = handler_general.get_integrate_github_user_by_name("skariel");
        final boolean indiepocalypse_is_admin = true;
        model_user_extended_info extended_info_indiepocalypse = model_user_extended_info.create(indiepocalypse.user_name, indiepocalypse_is_admin);
        final boolean skariel_is_admin = true;
        model_user_extended_info extended_info_skariel = model_user_extended_info.create(skariel.user_name, skariel_is_admin);
        store_local_db.update_user_extended_info(extended_info_indiepocalypse);
        store_local_db.update_user_extended_info(extended_info_skariel);

        t1 = new Thread() {
            public void run() {
                while (!interrupted) {
                    try {
                        if ((!initially_synced) && (!interrupted)) {
                            sync();
                            initially_synced = true;
                        } else {
                            return;
                        }
                        if (!interrupted) {
                            Thread.sleep(store_conf.get_delay_L3_milis());
                        } else {
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

    private static void stop() {
        interrupted = true;
        if (t1 != null) {
            t1.interrupt();
            t1 = null;
        }
    }

    private static void sync() {
        List<model_user> users = store_local_db.get_all_users();
        Logger.info("syncing " + Integer.toString(users.size()) + " users with github");
        for (model_user user : users) {
            try {
                if (!interrupted) {
                    Thread.sleep(store_conf.get_delay_L1_milis());
                } else {
                    return;
                }
            } catch (Exception e) {
                if (!interrupted) {
                    Logger.error("while syncing user with github...", e);
                }
            }
            if (interrupted) {
                return;
            }
            try {
                user = store_github_api.get_user_by_name(user.user_name);
            } catch (Exception e) {
                Logger.error("while getting user info from github during user sync...", e);
            }
            store_local_db.update_user(user);
        }
    }
}
