import controllers.GmailInbox;
import controllers.github_repo_sync;
import play.Application;
import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        Logger.info("Starting gmail idle poll...");
        GmailInbox.start();
        github_repo_sync.start();
    }

    @Override
    public void onStop(Application app) {
        Logger.info("Stopping gmail idle poll...");
        GmailInbox.stop();
        github_repo_sync.stop();
    }
}
