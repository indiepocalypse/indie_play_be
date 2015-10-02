package controllers;

import play.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by skariel on 02/10/15.
 */
public class github_iojs {
    static public void accept_trasfer_repo(String url) {
        credentials credentials = new credentials();
        try {
            Process process = new ProcessBuilder(
                    "app/iojs/iojs", "app/iojs/accept_repo_transfer.js", credentials.name, credentials.pssw, url).start();
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line = null;
            while ((line = reader.readLine()) != null) {
                Logger.info("iojs => "+line);
            }
            // TODO: handle unsuccesful transfer. At least notify the user?
        }
        catch (Exception e) {
           Logger.error("while executing iojs...", e);
        }
    }
}