import play.Application;
import play.GlobalSettings;
import play.Logger;
import sync.sync_github_repos;
import sync.sync_gmail;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        Logger.info("Starting gmail idle poll...");
        sync_gmail.start();
        sync_github_repos.start();
    }

    @Override
    public void onStop(Application app) {
        Logger.info("Stopping gmail idle poll...");
        sync_gmail.stop();
        sync_github_repos.stop();
    }
}
