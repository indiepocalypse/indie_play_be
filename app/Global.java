import controllers.GmailInbox;
import play.*;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        Logger.info("Starting gmail idle poll...");
        GmailInbox.start();
    }
}