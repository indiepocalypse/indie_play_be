import play.*;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.libs.F;
import play.libs.F.*;
import play.mvc.Http.*;
import play.mvc.*;
import views.enum_main_page_type;
import views.html.view_main;

import javax.inject.*;

public class ErrorHandler extends DefaultHttpErrorHandler {
    private final Environment environment;

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment,
                        OptionalSourceMapper sourceMapper, Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
        this.environment = environment;
    }


    protected F.Promise<Result> onBadRequest(RequestHeader request, String message) {
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.badRequest(view_main.render("explore", enum_main_page_type.INDEX, message))
            );
        }
        else {
            return super.onBadRequest(request, message);
        }
    }

    protected F.Promise<Result> onForbidden(RequestHeader request, String message) {
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.forbidden(view_main.render("explore", enum_main_page_type.INDEX, message))
            );
        }
        else {
            return super.onForbidden(request, message);
        }
    }

    protected F.Promise<Result> onNotFound(RequestHeader request, String message){
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.notFound(view_main.render("explore", enum_main_page_type.INDEX, message))
            );
        } else {
            return super.onNotFound(request, message);
        }
    }

    protected F.Promise<Result> onOtherClientError(RequestHeader request, int statusCode, String message) {
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.status(statusCode, view_main.render("explore", enum_main_page_type.INDEX, message))
            );
        }
        else {
            return super.onOtherClientError(request, statusCode, message);
        }
    }


    protected Promise<Result> onProdServerError(RequestHeader request, UsefulException exception) {
        return Promise.<Result>pure(
                Results.internalServerError(view_main.render("explore", enum_main_page_type.EXPLORE, exception.getMessage()))
        );
    }
}
