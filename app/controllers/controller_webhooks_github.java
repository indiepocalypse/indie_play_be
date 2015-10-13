package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by skariel on 12/10/15.
 */
public class controller_webhooks_github extends Controller {
    public Result handle_wildcard() {
        // TODO: implement ;)
        Logger.info(request().body().asText());
        throw new NotImplementedException();
    }
}

