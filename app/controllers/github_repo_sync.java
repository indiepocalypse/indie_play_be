package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.repo_model;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 06/10/15.
 */
public class github_repo_sync {
    static Thread t1 = null;
    static boolean syncing = false;
    // TODO: move this constant to store and conf
    static final int delta_milis_sync = 60*1000*5; // 5 minutes for testing only...
    static public void start() {
        if (t1==null) {
            t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            sync();
                            Thread.sleep(delta_milis_sync);
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
        if (t1!=null) {
            t1.interrupt();
            t1 = null;
        }
    }

    static void sync() {
        if (syncing) {
            return;
        }
        syncing = true;

        WSClient ws;
        try {
            ws = WS.client();
        }
        catch (Exception ignored) {
            ws = WS.newClient(1);
        }
        WSResponse res = github_access.get_indie_repositories(ws).execute().get(60, TimeUnit.SECONDS);
        JsonNode json = play.libs.Json.parse(res.getBody());
        for (int i=0; i< json.size(); i++) {
            JsonNode json_repo = json.get(i);
            String name = json_repo.get("name").asText("");
            String description = json_repo.get("description").asText("");
            String github_html_url = json_repo.get("html_url").asText("");
            String homepage = json_repo.get("homepage").asText("");
            Integer stars_count = json_repo.get("stargazers_count").asInt(0);
            Integer forks_count = json_repo.get("forks_count").asInt(0);
            repo_model repo = new repo_model(name, description, homepage, github_html_url, stars_count, forks_count);
            store.update_repo(repo);
        }
        Logger.info("SYNCSYNC SIZE=" + Integer.toString(json.size()));
//XXXXXXXXXXXXXXXXXXXXX

        syncing = false;

    }
}
