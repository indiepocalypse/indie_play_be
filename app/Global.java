import com.typesafe.config.ConfigException;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import stores.store_conf;
import sync.sync_github_repos;
import sync.sync_github_users;
import sync.sync_gmail;

import javax.annotation.Nonnull;
import java.io.File;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        super.onStart(app);

        // TODO: this is just stub. DOC files should be parsed and generate some nice html, etc.
        File directory = app.getFile("./conf/internal_resources/docs"); //new File("/app");
        @Nonnull final File[] fList = directory.listFiles();
        assert fList != null;
        try {
            for (File file : fList) {
                Logger.info(" ------ " + file.getPath() + " ---------- " + file.getName());
            }
        } catch (NullPointerException e) {
            Logger.error("While listing files in interbal resources. This should not happen");
        }


        Logger.info("global start called");
        if (store_conf.get_debug_should_check_mails()) {
            Logger.info("starting gmail sync");
            sync_gmail.start();
        } else {
            Logger.info("skipping gmail sync due to debug env option");
        }
        Logger.info("starting github repos sync");
        sync_github_repos.start();
        Logger.info("starting github users sync");
        sync_github_users.start();
    }
}
