package controllers;

import models.ownership_model;
import models.repo_model;
import models.user_model;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApplicationRoutes extends Controller {
    // TODO: should login redirect to the current page always? currently only doing for create new repo page
    // TODO: add some jitter to Gmail and other syncing activities...
    // TODO: cache the simple pages (e.g. the landing page)

    final static String main_title = "it's the Indiepocalypse!";
    @Inject
    public WSClient ws;

    private boolean is_redirected_from_github_login() {
        return request().getQueryString("code") != null;
    }

    public Result faq() {
        return ok(main.render("faq", "This is the FAQ!", this));
    }

    public F.Promise<Result> explore() {
        return F.Promise.promise(() -> {
            List<repo_model> repos = store.get_all_repos();
            List<user_model> users = store.get_all_users();
            return ok(main.render("explore", repo_explore.render(repos, users), this));
        });
    }

    public Result newrepo_get() {
        String def_repo_name = "";
        String def_repo_homepage = "";
        String def_repo_description = "";
        String err = null;
        return ok(main.render("new repo", newrepo.render(this, def_repo_name, def_repo_homepage, def_repo_description, err), this));
    }

    public Result newrepo_post() {
        DynamicForm data = Form.form().bindFromRequest();

        String repo_name = "";
        try {
            repo_name = data.get(store.repo_name_name);
        } catch (Exception ignore) {
        }

        String repo_homepage = "";
        try {
            repo_homepage = data.get(store.repo_homepage_name);
        } catch (Exception ignore) {
        }

        String repo_description = "";
        try {
            repo_description = data.get(store.repo_description_name);
        } catch (Exception ignore) {
        }

        try {
            repo_model repo = github_access.create_new_repo(ws, repo_name, repo_homepage, repo_description);
            store.register_new_repo(this, repo);
            return redirect("/r/" + repo_name);
        }
        catch (Exception e) {
            String err = "Couldn't create the repo, sorry!\n" +
                    "this is the reported result:\n\n" + e.getMessage();
            // TODO: report a better arror, at least format it or whatever...
            return ok(main.render("new repo", newrepo.render(this, repo_name, repo_homepage, repo_description, err), this));
        }
    }

    public Result blog() {
        return ok(main.render("blog", "there are " + Integer.toString(GmailInbox.mail_count) + " messages in inbox!", this));
        //return ok(main.render("blog", "This is the blog!", this));
    }

    public Result settings() {
        return ok(main.render("settings", "This is user settings!", this));
    }

    public Result user_profile(String user_name) {
        return ok(main.render(user_name, homeuser.render(this, store.get_user_by_name(user_name)), this));
    }

    public Result repo_profile(String repo_name) {
        List<ownership_model> owners = store.get_ownerships_by_repo_name(repo_name);
        return ok(main.render(repo_name, homerepo.render(this, store.get_repo_by_name(repo_name), owners), this));
    }

    public Result pull_profile(String repo_name, Long pull_id) {
        String pull_id_str = Long.toString(pull_id);
        return ok(main.render(repo_name + "@" + pull_id_str, "This is the pull id " + pull_id_str + " in repo " + repo_name, this));
    }

    public F.Promise<Result> index() {
        if (store.user_is_logged(this)) {
            return F.Promise.promise(() -> {
                if (store.has_returnto(this)) {
                    Logger.info(store.pop_return_to(this));
                    Logger.info(store.pop_return_to(this));
                    return redirect(store.pop_return_to(this));
                }
                return ok(main.render(main_title, "Welcome!", this));
            });
        }

        if (is_redirected_from_github_login()) {
            // ie use is in the  process of logging in
            String code = request().getQueryString("code");
            String state = request().getQueryString("state");
            if (state.equals(store.get_state(this))) {
                store.set_github_code(this, code);
                return F.Promise.promise(() -> {
                    String token = github_access.get_github_access_token(state, code);
                    if (token==null) {
                        return unauthorized();
                    }
                    // user has logged in!
                    store.set_token(this, token);
                    user_model user = github_access.get_user_by_token(token);
                    store.set_current_user(this, user);
                    store.update_user(user);
                    return index().get(60, TimeUnit.SECONDS);
                });
            }
            return F.Promise.promise(() -> unauthorized());
        }

        // user is not in the proceess of logging in
        if (store.get_state(this) == null) {
            store.set_state(this, github_access.get_random_string());
        }
        return F.Promise.promise(() -> ok(main.render(main_title, landing.render(), this)));
    }

    public Result logout() {
        store.clear(this);
        return redirect("/");
    }
}

