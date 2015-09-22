package controllers;

import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.*;

import play.twirl.api.Html;
import views.html.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 22/09/15.
 */
public class API extends Controller {
    // TODO: implement some api!
    // wanted api:
    // -- get user (avatar uri, name, mail)
    // -- get user repos (repo name, uri)
    // -- get user shares in indie repos (repo for each share, etc)
    // -- get indie repos (shares, owners, etc.)
    // -- get pull requests for each repo
    // -- get user/repo voting history/status
    // much more...


}
