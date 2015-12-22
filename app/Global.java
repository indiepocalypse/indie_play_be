import play.Application;
import play.GlobalSettings;
import play.Logger;
import stores.store_conf;
import sync.sync_github_repos;
import sync.sync_github_users;
import sync.sync_gmail;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        super.onStart(app);
        Logger.info("global start called");
        if (store_conf.get_debug_should_check_mails()) {
            Logger.info("starting gmail sync");
            sync_gmail.start();
        }
        else {
            Logger.info("skipping gmail sync due to debug env option");
        }
        Logger.info("starting github repos sync");
        sync_github_repos.start();
        Logger.info("starting github users sync");
        sync_github_users.start();
    }
}
