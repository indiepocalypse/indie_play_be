package controllers;

import handlers.handler_general;
import handlers.handler_policy;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_db_indie.*;
import org.markdown4j.Markdown4jProcessor;
import org.smx.captcha.Producer;
import org.smx.captcha.impl.FactoryLanguageImpl;
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

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public class controller_main extends Controller {
    // TODO: use reverse routing so I don't repeat myself :) (done?!)
    // TODO: summarizing classes and models (like sync but internal, also cleaning stuff etc.)
    // TODO: is the save/update dichotomy in the store_local_db really necessary? maybe update is enough?
    // TODO: error handling: when contacting github and the db touch 1st github and only if success continue
    // TODO: pretty-print the BigDecimals
    // TODO: fix double "%" printing when making an offer or request
    // TODO: refactor out content pages into a handler, all cached. This controller should not touch caching
    // TODO: view caching
    // TODO: a nice generic error page with login option, etc.

    private final static String main_title = "it's the Indiepocalypse!";
    private boolean is_redirected_from_github_login() {
        return request().getQueryString("code") != null;
    }

    public final static String EXPLORE_PAGE_CONTENT_CACHE_KEY = "exlpore_webpage_content";


    public Result docs() {
        return ok(view_main.render("docs", enum_main_page_type.DOCS, view_docs.render()));
    }

    public Result explore() {
        Html explore_page_content = (Html) Cache.getOrElse(EXPLORE_PAGE_CONTENT_CACHE_KEY, (Callable<Object>) () -> {
            Logger.info("GENERATING EXPLORE PAGE CONTENT CACHE!!!!!!!!!!!!!!!!!");
            List<model_repo> repos = store_local_db.get_all_repos();
            List<model_user> users = store_local_db.get_all_users();
            Map<String, List<model_repo_image>> repo_images_map = store_local_db.get_map_all_repo_names_to_images();
            // we have to separate the repos into rows of 4 items..
            List<ArrayList<model_repo>> list_repos = new ArrayList<>(1);
            for (model_repo repo: repos) {
                if ((list_repos.size()==0) || (list_repos.get(list_repos.size()-1).size()>=4)) {
                    list_repos.add(new ArrayList<>(4));
                }
                list_repos.get(list_repos.size()-1).add(repo);
            }
            return view_repo_explore.render(list_repos, users, repo_images_map);
        }, (int) store_conf.get_delay_L2_seconds());
        return ok(view_main.render("explore", enum_main_page_type.EXPLORE, explore_page_content));
    }

    public Result newrepo_get() {
        if (!store_session.user_is_logged()) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, "you must be logged in to create a new repo"));
        }
        if (!handler_policy.can_create_new_repo()) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo_too_many.render()));
        }
        if ((store_session.user_is_logged()) && (handler_policy.is_rate_limited(store_session.get_user_name()))) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo_rate_limited.render()));
        }
        String def_repo_name = "";
        String def_repo_homepage = "";
        String def_repo_description = "";
        return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(def_repo_name, def_repo_homepage, def_repo_description, null)));
    }

    public Result newrepo_post() {
        DynamicForm data = Form.form().bindFromRequest();

        @Nonnull String repo_name = "";
        try {
            repo_name = data.get(store_session.repo_name_name);
        } catch (Exception ignore) {
        }
        if (repo_name==null) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render("", "", "", "repo name field is missing")));
        }
        assert repo_name != null;

        @Nonnull String repo_homepage = "";
        try {
            repo_homepage = data.get(store_session.repo_homepage_name);
        } catch (Exception ignore) {
        }
        if (repo_homepage==null) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render("", "", "", "repo homepage field is missing")));
        }
        assert repo_homepage != null;

        @Nonnull String repo_description = "";
        try {
            repo_description = data.get(store_session.repo_description_name);
        } catch (Exception ignore) {
        }
        if (repo_description==null) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render("", "", "", "repo description field is missing")));
        }
        assert repo_description != null;

        if (!store_session.user_is_logged()) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, "")));
        }
        if (handler_policy.is_rate_limited(store_session.get_user_name())) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, "you hit the rate limit, please try again in a few minutes")));
        }
        if (store_local_db.has_repo(repo_name)) {
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, "repo name already exists. Please choose another")));
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

            // register the interaction
            assert store_session.user_is_logged();
            model_user_interaction model_user_interaction = models_db_indie.model_user_interaction.from_web(store_session.get_user_name(), enum_user_interaction_web_type.NEW_REPO);
            store_local_db.update_user_interaction(model_user_interaction);

            return redirect(routes.controller_main.repo_profile(repo_name));
        } catch (Exception e) {
            Logger.error("while creating repo...", e);
            String err = "Couldn't create the repo, sorry!\n" +
                    "this is the reported result:\n\n" + e.getMessage();
            // TODO: report a better arror, at least format it or whatever...
            return ok(view_main.render("new repo", enum_main_page_type.INDEX, view_newrepo.render(repo_name, repo_homepage, repo_description, err)));
        }
    }

    public Result image_get(String unique_file_name) {
        model_image model_image = store_local_db.get_image_by_id(unique_file_name);
        if (model_image==null) {
            return ok("no image for this repo");
        }
        response().setHeader("Content-Type", "image");
        return ok(model_image.getImage());
    }

    public Result captcha_image_get() {
        // TODO: integrate with session
        Properties props = new Properties();
        props.put("format", "jpg");
        props.put("font", "Helvetica");
        props.put("fontsize", "28");
        props.put("min-width", "180");
        props.put("padding-x", "25");
        props.put("padding-y", "25");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            FactoryLanguageImpl inst = (FactoryLanguageImpl) Producer.forName("org.smx.captcha.impl.FactoryLanguageImpl");
            inst.setLanguageDirectory("./conf/internal_resources/icaptcha_langs");
            inst.setLanguage("EN");
            inst.setRange(5, 10); //Select words between 5-10 letters
            Producer.render(os, inst, props);
        }
        catch (Exception e) {
            Logger.error("while generating captcha: ",e);
            return ok(view_main.render("captcha error", enum_main_page_type.INDEX, "error while generating captcha. Please contact an admin"));
        }
        response().setHeader("Content-Type", "image");
        return ok(os.toByteArray());
    }

    public Result repo_image_upload_get(String repo_name) {
        // TODO: use the parameter
        return ok(view_main.render("upload_image", enum_main_page_type.INDEX, view_repo_image_upload.render()));
    }

    public Result repo_image_upload_post(String repo_name) {
        if (!store_session.user_is_logged()) {
            return ok(view_main.render("image upload", enum_main_page_type.INDEX, "you have to be logged in to upload an image"));
        }
        final handler_policy.can_upload_image_result can_upload = handler_policy.can_upload_image(store_session.get_user_name(), repo_name);
        if (!can_upload.can_upload) {
            String text = "";
            switch (can_upload.enum_result) {
                case NULL_NAME:
                    text = "a null user name was found";
                    break;
                case NULL_OWNERHSIP:
                    text = "null ownership was found";
                    break;
                case NOT_ENOUGH_OWNERSHIP:
                    text = "ownership required to upload an image per repository policy is "+can_upload.required_ownership.toString()+"%"+" but you have only "+can_upload.user_ownership.toString()+"%";
                    break;
                case NO_REPO_POLICY:
                    text = "no repo policy on image uploads";
                    break;
                case SEE_CAN_UPLOAD_FIELD:
                    text = "some problem with no good description happened, please contact an admin";
                    break;
            }
            text = "problem uploading image to repo " + repo_name + ": " + text;
            return ok(view_main.render("image upload", enum_main_page_type.INDEX, text));
        }
        play.mvc.Http.MultipartFormData body = request().body().asMultipartFormData();
        play.mvc.Http.MultipartFormData.FilePart image = body.getFile("image");
        if (image != null) {
            java.io.File file = image.getFile();
            // TODO: limit file size!
            // TODO: check user can actually upload to that repo...
            try {
                @Nonnull final byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                assert bytes != null;
                Logger.info("image lenbytes=" + Integer.toString(bytes.length));

                model_image model_image =  new model_image(store_session.get_user_name(), bytes);
                model_repo_image model_repo_image = new model_repo_image(repo_name, model_image.unique_file_name);
                // TODO: start transaction
                store_local_db.update_image(model_image);
                store_local_db.update_repo_image(model_repo_image);
                // TODO: end transaction
                // TODO: ad transactions in all places!
                return ok("File uploaded, user name is " + store_session.get_user_name() + " file name: " + model_image.unique_file_name);
            } catch (Exception e) {
                Logger.error("while reading all byte from image ", e);
                // TODO: make a dedicated error page for this stuff. It's all over the place
                return internalServerError();
            }
        } else {
            flash("error", "Missing file");
            return badRequest();
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
        Map<model_user, model_ownership> owners = store_local_db.get_users_and_ownerships_by_repo_name(repo_name);
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
                model_user user;
                try {
                    user = store_github_api.get_user_by_token(token);
                } catch (github_io_exception e) {
                    return ok(view_main.render(main_title, enum_main_page_type.INDEX, "error while logging in. Couldn't read user info from github"));
                }
                store_session.set_admin(store_local_db.is_admin(user.user_name));
                store_session.set_current_user(user);
                store_local_db.update_user(user);

                // register interaction
                model_user_interaction model_user_interaction = models_db_indie.model_user_interaction.from_web(user.user_name, enum_user_interaction_web_type.LOGIN);
                store_local_db.update_user_interaction(model_user_interaction);

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

    public Result about() {
        // TODO: implement!
        return ok(view_main.render("About", enum_main_page_type.INDEX, "This is the about..."));
    }

    public Result privacy_policy() {
        // TODO: implement!
        return ok(view_main.render("Privacy Policy", enum_main_page_type.INDEX, "This is the privacy policy..."));
    }

    public Result terms_and_conditions() {
        // TODO: implement!
        return ok(view_main.render("Terms and Conditions", enum_main_page_type.INDEX, "This is the terms and conditions..."));
    }

    public Result contact() {
        // TODO: implement!
        return ok(view_main.render("Contact", enum_main_page_type.INDEX, view_contact.render()));
    }

    public Result logout() {
        // we have to check. Otherwise get_user_name below will throw
        if (!store_session.user_is_logged()) {
            return redirect("/");
        }
        @Nonnull final String user_name = store_session.get_user_name();
        // register interaction
        model_user_interaction model_user_interaction = models_db_indie.model_user_interaction.from_web(user_name, enum_user_interaction_web_type.LOGOUT);
        store_local_db.update_user_interaction(model_user_interaction);

        store_session.clear();
        return redirect("/");
    }
}

