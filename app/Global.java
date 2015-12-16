import play.Application;
import play.GlobalSettings;
import play.Logger;
import sync.sync_github_repos;
import sync.sync_github_users;
import sync.sync_gmail;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        super.onStart(app);
        Logger.info("global start called");
        Logger.info("starting gmail sync");
        sync_gmail.start();
        Logger.info("starting github repos sync");
        sync_github_repos.start();
        Logger.info("starting github users sync");
        sync_github_users.start();
    }
}
