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
        // TODO: store in github webhoos store
        // TODO: create a github webhooks store
        // TODO: remember last webhook date, sync from last date on startup
        Logger.info("** incomming webhook! **");
        Logger.info(request().body().asJson().toString());
        return ok();
    }
}

