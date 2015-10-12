package stores;

import play.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by skariel on 02/10/15.
 */
public class store_github_iojs {
    static public boolean accept_trasfer_repo(String url) {
        // returns success!
        try {
            Process process = new ProcessBuilder(
                    "app/iojs/iojs", "app/iojs/accept_repo_transfer.js", store_credentials.github.name, store_credentials.github.pssw, url)
                    .start();
            // Reading the result... not used right now
//            InputStream in = process.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                Logger.info("iojs => " + line);
//            }
//            while (reader.readLine()!=null) {} // just read everuthing.
        } catch (Exception e) {
            Logger.error("while executing iojs...", e);
            return false;
        }
        return true;
    }
}
