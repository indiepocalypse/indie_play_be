import controllers.GmailInbox;
import play.*;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        Logger.info("Starting gmail idle poll...");
        GmailInbox.start();
    }

    @Override
    public void onStop(Application app) {
        Logger.info("Stopping gmail idle poll...");
        GmailInbox.stop();
    }
}
