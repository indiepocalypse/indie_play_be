package stores;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import models.model_repo;
import models.model_user;
import models_github.model_issue;
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
    // TODO: move this scope constant to conf
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

    public static List<model_repo> get_indie_repositories() {
        WSResponse res = indie_auth_request("/user/repos")
                .setMethod("GET")
                .execute()
                .get(60, TimeUnit.SECONDS);
        JsonNode json = play.libs.Json.parse(res.getBody());
        ArrayList<model_repo> repos = new ArrayList<>(json.size());
        for (int i = 0; i < json.size(); i++) {
            JsonNode json_repo = json.get(i);
            repos.add(model_repo.from_json(json_repo));
        }
        return repos;
    }

    public static model_repo create_new_repo(String repo_name, String repo_homepage, String repo_description) throws Exception {
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
        if (res.getStatus() == 201) {
            return model_repo.from_name_desc_and_homepage(repo_name, repo_description, repo_homepage);
        }
        throw new Exception(res.getBody());
    }

    public static model_repo get_repo_by_name(String user_name, String repo_name) {
        String path = "/repos/" + user_name + "/" + repo_name;
        WSRequest req = indie_auth_request(path);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        return model_repo.from_json(play.libs.Json.parse(res.getBody()));
    }

    public static model_user get_user_by_token(String token) {
        WSResponse res_user;
        WSRequest req_user = user_auth_request(token, "/user")
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        return model_user.from_json(Json.parse(res_user.getBody()));
    }

    public static model_user get_user_by_name(String name) {
        WSResponse res_user;
        WSRequest req_user = indie_auth_request("/users/" + name)
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        return model_user.from_json(Json.parse(res_user.getBody()));
    }

    public static boolean has_webhook(model_repo repo) {
        String path = "/repos/__OWNER__/__REPO__/hooks"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name);
        WSRequest req = indie_auth_request(path).setMethod("GET");
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        JsonNode json = play.libs.Json.parse(res.getBody());
        if (json.size() > 0) {
            Logger.info("repo "+repo.repo_name+" already has a webhook");
        }
        else {
            Logger.info("repo "+repo.repo_name+" doesn't have a webhook");
        }
        return json.size() > 0;
    }

    public static boolean create_webhook(model_repo repo) {
        Logger.info("creating webhook for repo named "+repo.repo_name);
        // (returns success)
        // see for reference: https://developer.github.com/v3/repos/hooks/
        String json_payload_to_create = "{\n" +
                "  \"name\": \"web\",\n" +
                "  \"active\": true,\n" +
                "  \"events\": \"*\",\n"+
                "  \"config\": {\n" +
                "    \"url\": \"__GITHUB_WEBHOOK_URL__\",\n" +
                "    \"content_type\": \"json\"\n" +
                "  }\n" +
                "}";
        json_payload_to_create = json_payload_to_create.replace("__GITHUB_WEBHOOK_URL__", store_conf.get_github_webhook_url());
        Logger.info("JJJJJJJ="+json_payload_to_create);
        String path = "/repos/__OWNER__/__REPO__/hooks"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name);
        WSRequest req = post_indie_auth_request(path, json_payload_to_create);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        boolean success = (res.getStatus() == 201)&&(res.getBody().contains("ping_url"));
        if (success) {
            Logger.info("successfuly created a webhook for repo named "+repo.repo_name);
            return true;
        }
        else {
            Logger.info("error during github webhook creation: "+res.getBody());
            return false;
        }
    }

    public static boolean comment_on_issue(model_repo repo, model_issue issue, String comment_body) {
        // returns success as usual...
        String json_payload_to_create = "{\"body\": \"__COMMENT_BODY__\"}".replace("__COMMENT_BODY__", comment_body);
        String path = "/repos/__OWNER__/__REPO__/issues/__NUMBER__/comments"
                .replace("__OWNER__", store_credentials.github.name)
                .replace("__REPO__", repo.repo_name)
                .replace("__NUMBER__", Integer.toString(issue.number));
        WSRequest req = post_indie_auth_request(path, json_payload_to_create);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        boolean success = (res.getStatus() == 201)&&(res.getBody().contains("created"));
        if (success) {
            Logger.info("successfuly created a comment for repo "+repo.repo_name+" for issue titled \""+issue.title+"\"");
            return true;
        }
        else {
            Logger.info("error during creating comment for repo \""+repo.repo_name+"\" for issue titled \""+issue.title+"\"");
            return false;
        }

    }
}
