package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.repo_model;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
                            Thread.sleep(store.get_github_repo_sync_delta_milis());
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

        List<repo_model> repos = github_access.get_indie_repositories();
        for (repo_model repo: repos) {
            store.update_repo(repo);
        }
        Logger.info("SYNCSYNC SIZE=" + Integer.toString(repos.size()));

        syncing = false;
    }
}
