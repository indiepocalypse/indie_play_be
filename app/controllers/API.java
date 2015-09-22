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
    // -- get github_user (avatar uri, name, mail)
    // -- get github_user repos (repo name, uri)
    // -- get github_user shares in indie repos (repo for each share, etc)
    // -- get indie repos (shares, owners, etc.)
    // -- get pull requests for each repo
    // -- get github_user/repo voting history/status
    // much more...


}
