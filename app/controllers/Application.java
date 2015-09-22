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

public class Application extends Controller {

    public Result index() {
        // TODO: return a nice, mostly static page
        if (session().get("token")!=null) {
            // user is logged in
            // TODO: make consts of all those routings...
            return redirect("/logged_in");
        }
        return ok(main.render("title!", Html.apply("<a href=\"/login\">Please login</a>")));
    }
    public Result login_with_github() {
        if (session().get("token")==null) {
            String state = github_access.get_random_string();
            session().put("state", state);
            return redirect(github_access.get_github_access_url(state));
        }
        return redirect(github_access.uri_logged_in);
    }
    public F.Promise<Result> just_received_code() {
        String code = request().getQueryString("code");
        String state = request().getQueryString("state");
        if (state.equals(session().get("state"))) {
            session().put("github_code", code);
            return F.Promise.promise(() -> {
                WSResponse res = github_access.get_github_access_token(state, code).execute().get(60, TimeUnit.SECONDS);

                String body = res.getBody();
                String[] splitted = body.split("\\&");
                if ((splitted.length != 3) || (!splitted[0].contains("=")) || (!splitted[1].contains("=")) || (!splitted[2].contains("="))) {
                    return unauthorized();
                }
                String token = splitted[0].split("\\=")[1];
                String token_type = splitted[2].split("\\=")[1];
                String scope = splitted[1].split("\\=")[1];
                session().put("token", token);
                session().put("token_type", token_type);
                session().put("scope", scope);
                return redirect(github_access.uri_logged_in);

            });
        }
        return F.Promise.promise(() -> unauthorized());
    }

    public F.Promise<Result> just_logged_in() {
        // TODO: extract functions `is_logged_in` etc.
        if (session().get("token")==null) {
            // user is not really logged  in
            return F.Promise.promise(()->redirect("/"));
        }
        return F.Promise.promise(()-> {
            // This is some (currently primitive) sync stuff. The user should eventually be able to trigger such
            // sync from the FE. Also this should happen the first time a user logs in and periodically.
            // so this should be refactore out, etc.
            WSResponse res_rep;
            WSResponse res_user;
            try {
                WSRequest req_rep = WS.url("https://api.github.com/user/repos")
                        .setHeader("Authorization", "token " + session().get("token"))
                        .setHeader("Accept", "application/vnd.github.v3 + json")
                        .setMethod("GET");
                F.Promise<WSResponse> pres_rep = req_rep.execute();
                WSRequest req_user = WS.url("https://api.github.com/user")
                        .setHeader("Authorization", "token " + session().get("token"))
                        .setHeader("Accept", "application/vnd.github.v3 + json")
                        .setMethod("GET");
                F.Promise<WSResponse> pres_user = req_user.execute();
                res_rep = pres_rep.get(60, TimeUnit.SECONDS);
                res_user = pres_user.get(60, TimeUnit.SECONDS);
                String avatar_url = Json.parse(res_user.getBody())
                        .get("avatar_url")
                        .asText();
                session().put("avatar_url", avatar_url);

            } catch (Exception e) {
                return ok(main.render("1111111", Html.apply(e.toString())));
            }
            // TODO: return a SPA (React, etc.) This should be the whole FE
            return ok(main.render("title!", Html.apply("<a href=\"/logout\">Logout</a>"+"<img src=\"" + session("avatar_url") + "\" alt=\"avatar\" style=\"width:304px;height:304px;\">" + "   Your repos: " + res_rep.getBody())));
        });
    }

    public Result logout() {
        session().clear();
        return redirect("/");
    }
}

