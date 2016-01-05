package stores;

import models_db_github.model_repo;
import play.Logger;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by skariel on 02/10/15.
 */
public class store_github_iojs {
    static private boolean run_iojs(String script_file_name, String... params) {
        try {
            ArrayList<String> args = new ArrayList<>(11);
            args.add("app/vendors/iojs/iojs");
            args.add("app/vendors/iojs/" + script_file_name);
            Collections.addAll(args, params);
            new ProcessBuilder(args).start();
            //Process process = new ProcessBuilder(args).start();
            // Reading the result... not used right now
//            InputStream in = process.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                Logger.info("iojs => " + line);
//            }
//            while (reader.readLine()!=null) {} // just read everuthing.

            return true;
        } catch (Exception e) {
            Logger.error("while running iojs...", e);
            return false;
        }
    }

    static public boolean accept_transfer_repo(String url) {
        return run_iojs("accept_repo_transfer.js", store_credentials.get_github_indie_user_name(), store_credentials.get_github_test_user_password(), url);
    }

    private static boolean create_file(model_repo repo, String file_name, String content) {
        String url = repo.github_html_url + "/new/master?";
        return run_iojs("create_file.js", store_credentials.get_github_indie_user_name(),
                store_credentials.get_github_indie_password(), url, file_name, content);
    }

    public static boolean create_readme(model_repo repo, String content) {
        final String file_name = "README.md";
        return create_file(repo, file_name, content);
    }
}
