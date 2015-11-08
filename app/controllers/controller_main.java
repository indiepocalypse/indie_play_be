package controllers;

import handlers.handler_general;
import handlers.handler_policy;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_db_indie.model_ownership;
import models_db_indie.model_repo_policy;
import org.markdown4j.Markdown4jProcessor;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import stores.store_github_api;
import stores.store_github_iojs;
import stores.store_local_db;
import stores.store_session;
import views.html.*;

import java.util.List;

public class controller_main extends Controller {
    // TODO: cache the simple pages (e.g. the landing page)
    // TODO: use reverse routing so I don't repeat myself :) (done?!)
    // TODO: summarizing classes and models (like sync but internal, also cleaning stuff etc.)
    // TODO: add a `remove` method in store_db (or a `delete` one?) (for pull_requests, offers, etc.)
    // TODO: is the save/update dichotomy in the store_local_db really necessary? maybe update is enough?
    // TODO: make a configuration model, cached, so it can be changed from the admin dashboard.
    // TODO: when merging, update repo from github. If offers were cleared, deny merge
    // TODO: policy commands
    // TODO: integrate policy into merging
    // TODO: integrate policy into issue lifecycle
    // TODO: implement policy changing commands

    private final static String main_title = "it's the Indiepocalypse!";
    private boolean is_redirected_from_github_login() {
        return request().getQueryString("code") != null;
    }

    public Result faq() {
        return ok(view_main.render("faq", view_faq.render()));
    }

    public Result explore() {
        List<model_repo> repos = store_local_db.get_all_repos();
        List<model_user> users = store_local_db.get_all_users();
        return ok(view_main.render("explore", view_repo_explore.render(repos, users)));
    }

    public Result newrepo_get() {
        if (!handler_policy.can_create_new_repo()) {
            return ok(view_main.render("new repo", view_newrepo_too_many.render()));
        }
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
        if (!store_session.user_is_logged()) {
            return ok(view_main.render("new repo", view_newrepo.render(repo_name, repo_homepage, repo_description, "")));
        }
        if (store_local_db.has_repo(repo_name)) {
            return ok(view_main.render("new repo", view_newrepo.render(repo_name, repo_homepage, repo_description, "repo name already exiss. Please choose another")));
        }
        if (!handler_policy.can_create_new_repo()) {
            return ok(view_main.render("new repo", view_newrepo_too_many.render()));
        }

        // create the repo, with proper ownership and policy!

        try {
            model_repo repo = store_github_api.create_new_repo(repo_name, repo_homepage, repo_description);
            model_user user = store_local_db.get_user_by_name(store_session.get_user_name());

            final boolean create_webhook = true;
            final boolean check_for_existance_of_readme_before_creating_one = false;
            final boolean delete_original_collaborators = false;
            model_ownership ownership = handler_general.integrate_github_repo(repo, user,
                    create_webhook, check_for_existance_of_readme_before_creating_one,
                    delete_original_collaborators);
            if (ownership==null) {
                handler_general.delete_repo_from_github_and_db_and_also_related_ownership_policy_offers(repo);
                // TODO: elaborate on error
                String err = "Couldn't create the repo, sorry!";
                return ok(view_main.render("new repo", view_newrepo.render(repo_name, repo_homepage, repo_description, err)));
            }
            store_session.set_new_repo(repo.repo_name);
            return redirect(routes.controller_main.repo_profile(repo_name));
        } catch (Exception e) {
            Logger.error("while creating repo...", e);
            String err = "Couldn't create the repo, sorry!\n" +
                    "this is the reported result:\n\n" + e.getMessage();
            // TODO: report a better arror, at least format it or whatever...
            return ok(view_main.render("new repo", view_newrepo.render(repo_name, repo_homepage, repo_description, err)));
        }
    }

    public Result blog() {
        // TODO: an actual blog view!
        String content = "error!"; //there are " + Integer.toString(sync_gmail.mail_count) + " messages in inbox!";
        try {
            content = new Markdown4jProcessor().process("This is a **bold** text\n\n```\nsome code...\nfn main() {}\n```");
        }
        catch (Exception e) {
            Logger.error("while rendering the blog...", e);
        }
        return ok(view_main.render("blog", view_blog_entry.render(new Html(content), store_session.user_is_admin())));
    }

    public Result settings() {
        return ok(view_main.render("settings", "This is user settings!"));
    }

    public Result user_profile(String user_name) {
        return ok(view_main.render(user_name, view_homeuser.render(store_local_db.get_user_by_name(user_name))));
    }

    public Result repo_profile(String repo_name) {
        List<model_ownership> owners = store_local_db.get_ownerships_by_repo_name(repo_name);
        List<model_pull_request> pull_requests = store_local_db.get_pull_requests_by_repo_name(repo_name);
        return ok(view_main.render(repo_name, view_homerepo.render(store_local_db.get_repo_by_name(repo_name), owners, pull_requests)));
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
                store_session.set_admin(store_local_db.is_admin(user.user_name));
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

