package controllers;

import models.model_ownership;
import models.model_repo;
import models.model_user;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import stores.store_github_api;
import stores.store_local_db;
import stores.store_session;
import sync.sync_gmail;
import views.html.*;

import java.math.BigDecimal;
import java.util.List;

public class controller_main extends Controller {
    // TODO: should login redirect to the current page always? currently only doing for create new repo page
    // TODO: cache the simple pages (e.g. the landing page)
    // TODO: remove original owner when repo is transferred
    // TODO: use reverse routing so I don't repeat myself :) (done?!)
    // TODO: enforce the following by structure: 1) only stores touch models, 2) only controller_main touche views (its already like this, but not enforced!)

    private final static String main_title = "it's the Indiepocalypse!";
    private boolean is_redirected_from_github_login() {
        return request().getQueryString("code") != null;
    }

    public Result faq() {
        return ok(view_main.render("faq", "This is the FAQ!"));
    }

    public F.Promise<Result> explore() {
        return F.Promise.promise(() -> {
            List<model_repo> repos = store_local_db.get_all_repos();
            List<model_user> users = store_local_db.get_all_users();
            return ok(view_main.render("explore", view_repo_explore.render(repos, users)));
        });
    }

    public Result newrepo_get() {
        String def_repo_name = "";
        String def_repo_homepage = "";
        String def_repo_description = "";
        return ok(view_main.render("new repo", view_newrepo.render(def_repo_name, def_repo_homepage, def_repo_description, null)));
    }

    public Result newrepo_post() {
        DynamicForm data = Form.form().bindFromRequest();

        String repo_name = "";
        try {
            repo_name = data.get(store_session.repo_name_name);
        } catch (Exception ignore) {
        }

        String repo_homepage = "";
        try {
            repo_homepage = data.get(store_session.repo_homepage_name);
        } catch (Exception ignore) {
        }

        String repo_description = "";
        try {
            repo_description = data.get(store_session.repo_description_name);
        } catch (Exception ignore) {

        }

        try {
            model_repo repo = store_github_api.create_new_repo(repo_name, repo_homepage, repo_description);
            store_session.set_new_repo(repo.repo_name);
            model_user user = store_github_api.get_user_by_name(store_session.get_user_name());
            model_ownership ownership = new model_ownership(user, repo, new BigDecimal("100.0"));
            store_local_db.update_ownership(ownership);
            return redirect(routes.controller_main.repo_profile(repo_name));
        } catch (Exception e) {
            String err = "Couldn't create the repo, sorry!\n" +
                    "this is the reported result:\n\n" + e.getMessage();
            // TODO: report a better arror, at least format it or whatever...
            return ok(view_main.render("new repo", view_newrepo.render(repo_name, repo_homepage, repo_description, err)));
        }
    }

    public Result blog() {
        // TODO: an actual blog view!
        return ok(view_main.render("blog", "there are " + Integer.toString(sync_gmail.mail_count) + " messages in inbox!"));
    }

    public Result settings() {
        return ok(view_main.render("settings", "This is user settings!"));
    }

    public Result user_profile(String user_name) {
        return ok(view_main.render(user_name, view_homeuser.render(store_local_db.get_user_by_name(user_name))));
    }

    public Result repo_profile(String repo_name) {
        List<model_ownership> owners = store_local_db.get_ownerships_by_repo_name(repo_name);
        return ok(view_main.render(repo_name, view_homerepo.render(store_local_db.get_repo_by_name(repo_name), owners)));
    }

    public Result pull_profile(String repo_name, Long pull_id) {
        String pull_id_str = Long.toString(pull_id);
        return ok(view_main.render(repo_name + "@" + pull_id_str, "This is the pull id " + pull_id_str + " in repo " + repo_name));
    }

    public Result index() {
        if (store_session.user_is_logged()) {
            if (store_session.has_returnto()) {
                return redirect(store_session.pop_return_to());
            }
            return ok(view_main.render(main_title, "Welcome!"));
        }

        if (is_redirected_from_github_login()) {
            // ie use is in the  process of logging in
            String code = request().getQueryString("code");
            String state = request().getQueryString("state");
            if (state.equals(store_session.get_state())) {
                store_session.set_github_code(code);
                String token = store_github_api.get_github_access_token(state, code);
                if (token == null) {
                    return unauthorized();
                }
                // user has logged in!
                store_session.set_token(token);
                model_user user = store_github_api.get_user_by_token(token);
                store_session.set_current_user(user);
                store_local_db.update_user(user);
                return index();
            }
            return unauthorized();
        }

        // user is not in the proceess of logging in
        if (store_session.get_state() == null) {
            store_session.set_state(store_github_api.get_random_string());
        }
        return ok(view_main.render(main_title, view_landing.render()));
    }

    public Result logout() {
        store_session.clear();
        return redirect("/");
    }
}

