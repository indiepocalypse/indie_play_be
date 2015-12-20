package controllers;

import handlers.handler_general;
import handlers.handler_policy;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_db_indie.model_ownership;
import org.markdown4j.Markdown4jProcessor;
import play.Logger;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import stores.*;
import views.enum_main_page_type;
import views.html.*;

import java.util.List;
import java.util.concurrent.Callable;

public class controller_main extends Controller {
    // TODO: use reverse routing so I don't repeat myself :) (done?!)
    // TODO: summarizing classes and models (like sync but internal, also cleaning stuff etc.)
    // TODO: is the save/update dichotomy in the store_local_db really necessary? maybe update is enough?
    // TODO: error handling: when contacting github and the db touch 1st github and only if success continue
    // TODO: pretty-print the BigDecimals
    // TODO: fix double "%" printing when making an offer or request
    // TODO: refactor out content pages into a handler, all cached. This controller should not touch caching
    // TODO: rethink caching strategy

    public final static String EXPLORE_PAGE_CONTENT_CACHE_KEY = "exlpore_webpage_content";
    private final static String main_title = "it's the Indiepocalypse!";

    private boolean is_redirected_from_github_login() {
        return request().getQueryString("code") != null;
    }

    public Result faq() {
        return ok(view_main.render("faq", enum_main_page_type.FAQ, view_faq.render()));
    }

    public Result explore() {
        Html explore_page_content = (Html) Cache.getOrElse(EXPLORE_PAGE_CONTENT_CACHE_KEY, (Callable<Object>) () -> {
            Logger.info("GENERATING EXPLORE PAGE CONTENT CACHE!!!!!!!!!!!!!!!!!");
            List<model_repo> repos = store_local_db.get_all_repos();
            List<model_user> users = store_local_db.get_all_users();
            return view_repo_explore.render(repos, users);
        }, (int) store_conf.get_delay_L2_seconds());
        return ok(view_main.render("explore", enum_main_page_type.EXPLORE, explore_page_content));
    }

    public Result newrepo_get() {
        // TODO: rate limit!
        if (!handler_policy.can_create_new_repo()) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo_too_many.render()));
        }
        String def_repo_name = "";
        String def_repo_homepage = "";
        String def_repo_description = "";
        return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(def_repo_name, def_repo_homepage, def_repo_description, null)));
    }

    public Result newrepo_post() {
        // TODO: rate limit!
        // TODO: make sure user can actually make the repo, maybe use the newrepo_get to put some flag in the session, so no need to touch the db
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
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, "")));
        }
        if (store_local_db.has_repo(repo_name)) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, "repo name already exiss. Please choose another")));
        }
        if (!handler_policy.can_create_new_repo()) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo_too_many.render()));
        }

        // create the repo, with proper ownership and policy!

        try {
            model_repo repo = store_github_api.create_new_repo(repo_name, repo_homepage, repo_description);
            model_user user = store_local_db.get_user_by_name(store_session.get_user_name());

            final boolean create_webhook = true;
            final boolean check_for_existance_of_readme_before_creating_one = false;
            final boolean delete_original_collaborators = false;
            handler_general.integrate_github_repo(repo, user,
                    create_webhook, check_for_existance_of_readme_before_creating_one,
                    delete_original_collaborators);
            store_session.set_new_repo(repo.repo_name);
            // invalidate explore page cache
            Cache.remove(EXPLORE_PAGE_CONTENT_CACHE_KEY);
            return redirect(routes.controller_main.repo_profile(repo_name));
        } catch (Exception e) {
            Logger.error("while creating repo...", e);
            String err = "Couldn't create the repo, sorry!\n" +
                    "this is the reported result:\n\n" + e.getMessage();
            // TODO: report a better arror, at least format it or whatever...
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, err)));
        }
    }

    public Result blog() {
        // TODO: an actual blog view!
        String content = "error!"; //there are " + Integer.toString(sync_gmail.mail_count) + " messages in inbox!";
        try {
            content = new Markdown4jProcessor().process("This is a **bold** text\n\n```\nsome code...\nfn main() {}\n```");
        } catch (Exception e) {
            Logger.error("while rendering the blog...", e);
        }
        return ok(view_main.render("blog", enum_main_page_type.BLOG, view_blog_entry.render(new Html(content), store_session.user_is_admin())));
    }

    public Result settings() {
        return ok(view_main.render("settings", enum_main_page_type.INDEX, "This is user settings!"));
    }

    public Result user_profile(String user_name) {
        // TODO: caching!
        return ok(view_main.render(user_name, enum_main_page_type.INDEX, view_homeuser.render(store_local_db.get_user_by_name(user_name))));
    }

    public Result repo_profile(String repo_name) {
        // TODO: caching!
        List<model_ownership> owners = store_local_db.get_ownerships_by_repo_name(repo_name);
        List<model_pull_request> pull_requests = store_local_db.get_pull_requests_by_repo_name(repo_name);
        return ok(view_main.render(repo_name, enum_main_page_type.INDEX, view_homerepo.render(store_local_db.get_repo_by_name(repo_name), owners, pull_requests)));
    }

    public Result pull_profile(String repo_name, Long pull_id) {
        // TODO: caching!
        String pull_id_str = Long.toString(pull_id);
        return ok(view_main.render(repo_name + "@" + pull_id_str, enum_main_page_type.INDEX, "This is the pull id " + pull_id_str + " in repo " + repo_name));
    }

    public Result index() {
        if (store_session.user_is_logged()) {
            if (store_session.has_returnto()) {
                return redirect(store_session.pop_return_to());
            }
            return ok(view_main.render(main_title, enum_main_page_type.INDEX, "Welcome!"));
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
                model_user user = null;
                try {
                    user = store_github_api.get_user_by_token(token);
                } catch (github_io_exception e) {
                    return ok(view_main.render(main_title, enum_main_page_type.INDEX, "error while logging in. Couldn't read user info from github"));
                }
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
        return ok(view_main.render(main_title, enum_main_page_type.INDEX, view_landing.render()));
    }

    public Result logout() {
        store_session.clear();
        return redirect("/");
    }
}

