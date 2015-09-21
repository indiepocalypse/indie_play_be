package controllers;

import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.*;

import play.twirl.api.Html;
import views.html.*;

import java.util.concurrent.TimeUnit;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Your app is ready."));
    }
    public Result login_with_github() {
        String state = github_access.get_random_string();
        session().put("state",state);
        return redirect(github_access.get_github_access_url(state));
    }
    public Result just_received_code() {
        String code = request().getQueryString("code");
        String state = request().getQueryString("state");
        if (state.equals(session().get("state"))) {
            session().put("github_code", code);
            WSResponse res = github_access.get_github_access_token(state, code).execute().get(60, TimeUnit.SECONDS);
            String body = res.getBody();
            String[] splitted = body.split("\\&");
            if ((splitted.length!=3) || (!splitted[0].contains("=")) || (!splitted[1].contains("=")) || (!splitted[2].contains("="))) {
                return unauthorized();
            }
            String token = splitted[0].split("\\=")[1];
            String token_type = splitted[2].split("\\=")[1];
            String scope = splitted[1].split("\\=")[1];
            session().put("token", token);
            session().put("token_type", token_type);
            session().put("scope", scope);
            return redirect(github_access.uri_logged_in);
        }
        return unauthorized();
    }

    public Result just_logged_in() {

        //return ok(main.render("title!", Html.apply("Your repos: "+session().get("token"))));//res.getBody()+" status: "+res.getStatusText())));
//        System.console().printf("11111111111111111111111111111111111111111\n");
        WSRequest req = WS.url("https://api.github.com/user/repos")
                .setHeader("Authorization", "token " + session().get("token"))
                .setHeader("Accept", "application/vnd.github.v3 + json")
                .setMethod("GET");
//        System.console().printf("11111111111111111111111111111111111111111\n");
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
//        System.console().printf("2222222222222222222222222222222222222222222222\n");
        return ok(main.render("title!", Html.apply("Your repos: "+res.getBody())));
    }
}

