import play.*;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.libs.F.*;
import play.mvc.Http.*;
import play.mvc.*;
import views.enum_main_page_type;
import views.html.view_main;

import javax.inject.*;

public class ErrorHandler extends DefaultHttpErrorHandler {

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment,
                        OptionalSourceMapper sourceMapper, Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
    }

    protected Promise<Result> onProdServerError(RequestHeader request, UsefulException exception) {
        return Promise.<Result>pure(
                Results.internalServerError(view_main.render("explore", enum_main_page_type.EXPLORE, exception.getMessage()))
        );
    }

    protected Promise<Result> onForbidden(RequestHeader request, String message) {
        return Promise.<Result>pure(
                Results.forbidden("You're not allowed to access this resource.")
        );
    }
}
