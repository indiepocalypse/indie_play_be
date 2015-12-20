import play.Configuration;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;
import views.enum_main_page_type;
import views.html.view_error;
import views.html.view_main;

import javax.inject.Inject;
import javax.inject.Provider;

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
                    Results.badRequest(view_main.render("explore", enum_main_page_type.INDEX, view_error.render("Bad request")))
            );
        } else {
            return super.onBadRequest(request, message);
        }
    }

    protected F.Promise<Result> onForbidden(RequestHeader request, String message) {
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.forbidden(view_main.render("explore", enum_main_page_type.INDEX, view_error.render("Forbidden")))
            );
        } else {
            return super.onForbidden(request, message);
        }
    }

    protected F.Promise<Result> onNotFound(RequestHeader request, String message) {
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.notFound(view_main.render("explore", enum_main_page_type.INDEX, view_error.render("Could bot find path " + request.path())))
            );
        } else {
            return super.onNotFound(request, message);
        }
    }

    protected F.Promise<Result> onOtherClientError(RequestHeader request, int statusCode, String message) {
        if (environment.isProd()) {
            return Promise.<Result>pure(
                    Results.status(statusCode, view_main.render("explore", enum_main_page_type.INDEX, view_error.render("Client error")))
            );
        } else {
            return super.onOtherClientError(request, statusCode, message);
        }
    }


    protected Promise<Result> onProdServerError(RequestHeader request, UsefulException exception) {
        return Promise.<Result>pure(
                Results.internalServerError(view_main.render("explore", enum_main_page_type.EXPLORE, view_error.render(exception.getMessage())))
        );
    }
}
