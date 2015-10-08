package controllers;

import models.repo_model;
import models.user_model;
import play.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by skariel on 02/10/15.
 */
public class github_iojs {
    static public boolean accept_trasfer_repo(String url) {
        // returns success!
        try {
            Process process = new ProcessBuilder(
                    "app/iojs/iojs", "app/iojs/accept_repo_transfer.js", store.get_indie_github_name(), store.get_indie_github_pssw(), url).start();
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                Logger.info("iojs => " + line);
            }
            return true;
        } catch (Exception e) {
            Logger.error("while executing iojs...", e);
            return false;
        }
    }
}
