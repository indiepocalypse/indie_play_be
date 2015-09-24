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

public class ApplicationRoutes extends Controller {
    // TODO: better organize routes, seems too much redirecting is going on...+

    public boolean user_is_logged() {
        return session().get("token")!=null;
    }
    public String get_avatar_url() {
        return session().get("avatar_url");
    }
    public String get_user_name() {
        return session().get("user_name");
    }
    public String get_state() {
        return session().get("state");
    }

    boolean is_redirected_from_github_login() {
        return request().getQueryString("code") != null;
    }

    public Result faq() {
        return ok(main.render("faq", "This is the FAQ!", this));
    }

    public Result explore() {
        return ok(main.render("explore", "This is Explore!", this));
    }

    public Result blog() {
        return ok(main.render("blog", "This is the blog!", this));
    }

    public Result settings() {
        return ok(main.render("settings", "This is user settings!", this));
    }

    public Result user_profile(String user_name) {
        return ok(main.render(user_name, "This is the user profile of " + user_name, this));
    }

    public Result repo_profile(String repo_name) {
        return ok(main.render(repo_name, "This is the profile of repo " + repo_name, this));
    }

    public Result pull_profile(String repo_name, Long pull_id) {
        String pull_id_str = Long.toString(pull_id);
        return ok(main.render(repo_name + "@" + pull_id_str, "This is the pull id " + pull_id_str + " in repo " + repo_name, this));
    }

    public F.Promise<Result> index() {
        // TODO: return a nice, mostly static page
        if (user_is_logged()) {
            return F.Promise.promise(() -> {
                // TODO: This is some (currently primitive) sync stuff. The user should eventually be able to trigger such
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
                    String user_name = Json.parse(res_user.getBody())
                            .get("login")
                            .asText();
                    session().put("user_name", user_name);

                } catch (Exception e) {
                    return ok(main.render("1111111", Html.apply(e.toString()), this));
                }
                // TODO: return a SPA (React, etc.) This should be the whole FE
                return ok(main.render("title!", Html.apply("Your repos: " + res_rep.getBody()), this));
            });
        }

        if (is_redirected_from_github_login()) {
            // ie use is in the  process of logging in
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
                    session().put("token", token);
                    session().put("token_type", token_type);
                    return index().get(60, TimeUnit.SECONDS);
                });
            }
            return F.Promise.promise(() -> unauthorized());
        }

        // user is not in the proceess of logging in
        String state = github_access.get_random_string();
        session().put("state", state);
        return F.Promise.promise(()->ok(main.render("title!", null, this)));
    }

    public Result logout() {
        session().clear();
        return redirect("/");
    }
}

