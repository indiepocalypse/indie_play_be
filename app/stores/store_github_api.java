package stores;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import models_db_github.model_pull_request;
import models_db_github.model_repo;
import models_db_github.model_user;
import models_memory_github.model_issue;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utils.utils_general;
import utils.utils_random_string;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 21/09/15.
 */
public class store_github_api {

    ////////////////////////////////////////////////////
    //
    //  BUILDING BLOCKS
    //
    //////////////////////////////////////////////////////

    private static final String scope = "";

    public static String get_github_access_url(String state) {
        final String github_access = "https://github.com/login/oauth/authorize?client_id=__CLIENT_ID__&redirect_uri=__CALLBACK_URI__&scope=" + scope + "&state=__STATE__";
        final String client_id = store_credentials.github.getClient_id();
        final String callback_uri = ConfigFactory.load().getString("credentials.indie.github.login.callback");
        return github_access.replace("__STATE__", state)
                .replace("__CLIENT_ID__", client_id)
                .replace("__CALLBACK_URI__", callback_uri);
    }

    public static String get_github_access_token(String state, String code) {
        final String client_id = store_credentials.github.getClient_id();
        final String client_secret = store_credentials.github.getClient_secret();

        WSResponse res = utils_general.getwsclient().url("https://github.com/login/oauth/access_token")
                .setMethod("POST")
                .setQueryParameter("client_id", client_id)
                .setQueryParameter("client_secret", client_secret)
                .setQueryParameter("code", code)
                .setQueryParameter("state", state)
                .execute()
                .get(60, TimeUnit.SECONDS);
        String body = res.getBody();
        String[] splitted = body.split("\\&");
        if ((splitted.length != 3) || (!splitted[0].contains("=")) || (!splitted[1].contains("=")) || (!splitted[2].contains("="))) {
            return null;
        }
        return splitted[0].split("\\=")[1];
    }

    public static String get_random_string() {
        return new utils_random_string(12).nextString();
    }

    private static WSRequest indie_auth_request(String path) {
        WSClient ws = utils_general.getwsclient();
        return ws.url("https://api.github.com" + path)
                .setHeader("Authorization", "Basic " + store_credentials.github.getAuth())
                .setHeader("Accept", "application/vnd.github.v3 + json");
    }

    private static WSRequest post_indie_auth_request(String path, JsonNode json) {
        return indie_auth_request(path)
                .setMethod("POST")
                .setContentType("application/json; charset=utf-8")
                .setBody(json);
    }

    private static WSRequest put_indie_auth_request(String path, JsonNode json) {
        return indie_auth_request(path)
                .setMethod("PUT")
                .setContentType("application/json; charset=utf-8")
                .setBody(json);
    }

    private static WSRequest post_indie_auth_request(String path, String json) {
        return indie_auth_request(path)
                .setMethod("POST")
                .setContentType("application/json; charset=utf-8")
                .setBody(json);
    }

    private static WSRequest user_auth_request(String token, String path) {
        WSClient ws = utils_general.getwsclient();
        return ws.url("https://api.github.com" + path)
                .setHeader("Authorization", "token " + token)
                .setHeader("Accept", "application/vnd.github.v3 + json");
    }

    ////////////////////////////////////////////////////
    //
    //  REAL GITHUB API
    //
    //////////////////////////////////////////////////////

    public static List<model_repo> get_indie_repositories() throws github_io_exception {
        WSResponse res = indie_auth_request("/user/repos")
                .setMethod("GET")
                .setQueryParameter("type", "all")
                .execute()
                .get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 200) {
            throw new github_io_exception("cannot read repos from github");
        }
        JsonNode json = play.libs.Json.parse(res.getBody());
        ArrayList<model_repo> repos = new ArrayList<>(json.size());
        for (int i = 0; i < json.size(); i++) {
            JsonNode json_repo = json.get(i);
            repos.add(model_repo.from_json(json_repo));
        }
        return repos;
    }

    public static model_repo create_new_repo(String repo_name, String repo_homepage, String repo_description) throws github_io_exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        if (repo_name != null) {
            json.put("name", repo_name);
        }
        if (repo_description != null) {
            json.put("description", repo_description);
        }
        if (repo_homepage != null) {
            json.put("homepage", repo_homepage);
        }
        json.put("has_wiki", false);
        json.put("has_downloads", false);
        WSResponse res = post_indie_auth_request("/user/repos", json).execute().get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 201) {
            throw new github_io_exception(res.getBody());
        }
        return model_repo.from_name_desc_and_homepage(repo_name, repo_description, repo_homepage);
    }

    public static model_repo get_repo_by_name(String user_name, String repo_name) throws github_io_exception {
        String path = "/repos/" + user_name + "/" + repo_name;
        WSRequest req = indie_auth_request(path);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 200) {
            throw new github_io_exception("while creating github repo");
        }
        JsonNode json = play.libs.Json.parse(res.getBody());
        return model_repo.from_json(json);
    }

    public static model_user get_user_by_token(String token) throws github_io_exception {
        WSResponse res_user;
        WSRequest req_user = user_auth_request(token, "/user")
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        if (res_user.getStatus() != 200) {
            throw new github_io_exception("while getting user (by token) from github");
        }
        return model_user.from_json(Json.parse(res_user.getBody()));
    }

    public static model_user get_user_by_name(String name) throws github_io_exception {
        WSResponse res_user;
        WSRequest req_user = indie_auth_request("/users/" + name)
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        if (res_user.getStatus() != 200) {
            throw new github_io_exception("while getting user (by name) from github");
        }
        return model_user.from_json(Json.parse(res_user.getBody()));
    }

    public static void create_webhook(model_repo repo) throws github_io_exception {
        // TODO: give some app id to the webhook?
        Logger.info("creating webhook for repo named " + repo.repo_name);
        // (returns success)
        // see for reference: https://developer.github.com/v3/repos/hooks/
        String json_payload_to_create = "{\n" +
                "  \"name\": \"web\",\n" +
                "  \"active\": true,\n" +
                "  \"events\": \"*\",\n" +
                "  \"config\": {\n" +
                "    \"url\": \"__GITHUB_WEBHOOK_URL__\",\n" +
                "    \"content_type\": \"json\"\n" +
                "  }\n" +
                "}";
        json_payload_to_create = json_payload_to_create.replace("__GITHUB_WEBHOOK_URL__", store_conf.get_github_webhook_url());
        String path = "/repos/__OWNER__/__REPO__/hooks"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name);
        WSRequest req = post_indie_auth_request(path, json_payload_to_create);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        boolean success = (res.getStatus() == 201) && (res.getBody().contains("ping_url"));
        if (success) {
            Logger.info("successfuly created a webhook for repo named " + repo.repo_name);
            return;
        }
        if (res.getBody().contains("already exists")) {
            Logger.info("hook already exists for repo \"" + repo.repo_name + "\"");
            return;
        }
        Logger.info("error during github webhook creation: " + res.getBody());
        throw new github_io_exception("while creating github webhook for repo " + repo.repo_name);
    }

    public static void comment_on_issue(model_repo repo, String issue_num, String comment_body) throws github_io_exception {
        // returns success as usual...
        JsonNode json = JsonNodeFactory.instance.objectNode().put("body", comment_body);
        String path = "/repos/__OWNER__/__REPO__/issues/__NUMBER__/comments"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name)
                .replace("__NUMBER__", issue_num);
        WSRequest req = post_indie_auth_request(path, json);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        boolean success = (res.getStatus() == 201) && (res.getBody().contains("created"));
        if (!success) {
            Logger.error("while commenting on issue #" + issue_num + " at repo " + repo.repo_name, res.asJson().toString());
            throw new github_io_exception("while trying to comment");
        }
    }

    public static void update_issue(model_repo repo, model_issue issue) throws github_io_exception {
        // returns success as usual...
        // TODO: add labels, etc.
        JsonNode json = JsonNodeFactory.instance.objectNode()
                .put("state", issue.is_closed() ? "closed" : "open")
                .put("title", issue.title)
                .put("body", issue.body);
        String path = "/repos/__OWNER__/__REPO__/issues/__NUMBER__"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name)
                .replace("__NUMBER__", issue.number);
        WSRequest req = indie_auth_request(path)
                .setMethod("PATCH")
                .setBody(json);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        boolean success = (res.getStatus() == 200) && (res.getBody().contains("body"));
        if (!success) {
            Logger.error("while updating issue #" + issue.number + " at repo " + repo.repo_name, res.asJson().toString());
            throw new github_io_exception("while updating issue number " + issue.number + " on repo " + repo.repo_name);
        }
    }

    public static void update_pull_request(model_pull_request pull_request) throws github_io_exception {
        // returns success as usual...
        JsonNode json = JsonNodeFactory.instance.objectNode()
                .put("state", pull_request.is_closed() ? "closed" : "open")
                .put("title", pull_request.title)
                .put("body", pull_request.body);
        String path = "/repos/__OWNER__/__REPO__/pulls/__NUMBER__"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", pull_request.repo.repo_name)
                .replace("__NUMBER__", pull_request.number);
        WSRequest req = indie_auth_request(path)
                .setMethod("PATCH")
                .setBody(json);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        boolean success = (res.getStatus() == 200) && (res.getBody().contains("body"));
        if (!success) {
            Logger.error("while updating pull request #" + pull_request.number + " at repo " + pull_request.repo.repo_name, res.asJson().toString());
            throw new github_io_exception("while updating pull request number " + pull_request.number + " on repo " + pull_request.repo.repo_name);
        }
    }

    public static model_pull_request get_pull_request_by_repo_by_number(String repo_name, String number) throws github_io_exception {
        String path = "/repos/theindiepocalypse/" + repo_name + "/pulls/" + number;
        WSRequest req = indie_auth_request(path).setMethod("GET");
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 200) {
            throw new github_io_exception("while reading repo " + repo_name + " from github");
        }
        return model_pull_request.from_json(res.asJson());
    }

    public static List<model_pull_request> get_all_pull_requests(model_repo repo) throws github_io_exception {
        WSResponse res = indie_auth_request("/repos/theindiepocalypse/" + repo.repo_name + "/pulls")
                .setMethod("GET")
                .execute()
                .get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 200) {
            throw new github_io_exception("while reading all pull requests on repo " + repo.repo_name);
        }
        JsonNode json = play.libs.Json.parse(res.getBody());
        ArrayList<model_pull_request> pull_requests = new ArrayList<>(json.size());
        for (int i = 0; i < json.size(); i++) {
            JsonNode json_pull_request = json.get(i);
            pull_requests.add(model_pull_request.from_json(json_pull_request));
        }
        return pull_requests;
    }

    public static boolean has_readme(String repo_name) throws github_io_exception {
        WSResponse res = indie_auth_request("/repos/theindiepocalypse/" + repo_name + "/readme")
                .setMethod("GET")
                .execute()
                .get(60, TimeUnit.SECONDS);
        if (res.getStatus() == 200) {
            return true;
        }
        if (res.getStatus() == 404) {
            return false;
        }
        throw new github_io_exception("while checking for readme for repo " + repo_name);
    }

    public static String get_user_mail(String user_name) throws github_io_exception {
        WSResponse res_user;
        WSRequest req_user = indie_auth_request("/users/" + user_name)
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        if (res_user.getStatus() != 200) {
            Logger.error("while getting user mail from github: bad response (i.e. != 200)");
            throw new github_io_exception("while reading user " + user_name + " mail");
        }
        JsonNode json = res_user.asJson();
        JsonNode json_mail = json.get("email");
        return json_mail.asText();
    }

    public static void merge_pull_request(model_pull_request pull_request, String commit_message) throws github_io_exception {
        // will only succeed if pull request is mergeable.
        // returns success
        JsonNode json = JsonNodeFactory.instance.objectNode()
                .put("commit_message", commit_message)
                .put("sha", pull_request.SHA);
        String path = "/repos/__OWNER__/__REPO__/pulls/__NUMBER__/merge"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", pull_request.repo.repo_name)
                .replace("__NUMBER__", pull_request.number);
        WSRequest req = put_indie_auth_request(path, json);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 200) {
            Logger.error("while mergin pull request for repo " + pull_request.repo.repo_name + " #" + pull_request.number + ":\n",
                    res.asJson().toString());
            throw new github_io_exception("while trying to merge pull request " + pull_request.number + " on repo " + pull_request.repo.repo_name);
        }
    }

    public static void delete_repo(model_repo repo) throws github_io_exception {
        // returns success
        String path = "/repos/__OWNER__/__REPO__"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name);
        WSRequest req = indie_auth_request(path).setMethod("DELETE");
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 204) {
            Logger.error("while deleting repo " + repo.repo_name + " status=" + Integer.toString(res.getStatus()) + "\n" +
                    res.asJson().toString());
            throw new github_io_exception("while trying to delete repo " + repo.repo_name);
        }
    }

    private static List<model_user> get_all_collaborators(model_repo repo) throws github_io_exception {
        WSResponse res = indie_auth_request("/repos/theindiepocalypse/" + repo.repo_name + "/collaborators")
                .setMethod("GET")
                .execute()
                .get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 200) {
            throw new github_io_exception("while trying to get a list of all collaborator for repo " + repo.repo_name);
        }
        JsonNode json = res.asJson();
        ArrayList<model_user> collaborators = new ArrayList<>(json.size());
        for (int i = 0; i < json.size(); i++) {
            collaborators.add(model_user.from_json(json.get(i)));
        }
        return collaborators;
    }

    private static void delete_collaborator_from_repo(model_repo repo, model_user user) throws github_io_exception {
        WSResponse res = indie_auth_request("/repos/theindiepocalypse/" + repo.repo_name + "/collaborators/" + user.user_name)
                .setMethod("DELETE")
                .execute()
                .get(60, TimeUnit.SECONDS);
        if (res.getStatus() != 204) {
            throw new github_io_exception("while trying to delete collaborator " + user.user_name + " from repo " + repo.repo_name);
        }
    }

    public static void delete_all_collaborators_from_repo(model_repo repo) throws github_io_exception {
        boolean ok = true;
        for (model_user user : get_all_collaborators(repo)) {
            try {
                delete_collaborator_from_repo(repo, user);
            } catch (github_io_exception e) {
                ok = false;
            }
        }
        if (!ok) {
            throw new github_io_exception("while trying to delete all collaborators from repo " + repo.repo_name);
        }
    }
}
