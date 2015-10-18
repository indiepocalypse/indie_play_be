import handlers.handler_general;
import models.model_user;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import stores.store_credentials;
import sync.sync_github_repos;
import sync.sync_github_users;
import sync.sync_gmail;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        handler_general.get_integrate_github_user_by_name(store_credentials.github.name);
        Logger.info("Starting gmail idle poll...");
        sync_gmail.start();
        sync_github_repos.start();
        sync_github_users.start();
    }

    @Override
    public void onStop(Application app) {
        Logger.info("Stopping gmail idle poll...");
        sync_gmail.stop();
        sync_github_repos.stop();
        sync_github_users.stop();
    }
}
