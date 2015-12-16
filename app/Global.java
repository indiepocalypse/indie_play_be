import play.Application;
import play.GlobalSettings;
import play.Logger;
import sync.sync_github_repos;
import sync.sync_github_users;
import sync.sync_gmail;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        Logger.info("Starting gmail idle poll...");
        sync_gmail.start();
        sync_github_repos.start();
        sync_github_users.start();
    }

    @Override
    public void onStop(Application app) {
        Logger.info("global stop called");
        Logger.info("stopping gmail sync");
        sync_gmail.stop();
        Logger.info("stopping github repos sync");
        sync_github_repos.stop();
        Logger.info("stopping github users sync");
        sync_github_users.stop();
    }
}
