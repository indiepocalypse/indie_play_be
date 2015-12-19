import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import sync.sync_github_repos;
import sync.sync_github_users;
import sync.sync_gmail;
import views.enum_main_page_type;
import views.html.view_main;

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

    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        return F.Promise.promise(()->Results.internalServerError(view_main.render("error", enum_main_page_type.INDEX, t.toString())));
    }
}
