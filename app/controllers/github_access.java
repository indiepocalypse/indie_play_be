package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import models.repo_model;
import models.user_model;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by skariel on 21/09/15.
 */
public class github_access {
    // TODO: move this scope constant to conf
    private static final String scope = "";

    public static String get_github_access_url(String state) {
        final String github_access = "https://github.com/login/oauth/authorize?client_id=__CLIENT_ID__&redirect_uri=__CALLBACK_URI__&scope=" + scope + "&state=__STATE__";
        final String client_id = store.get_indie_github_client_id();
        final String callback_uri = ConfigFactory.load().getString("credentials.indie.github.login.callback");
        return github_access.replace("__STATE__", state)
                .replace("__CLIENT_ID__", client_id)
                .replace("__CALLBACK_URI__", callback_uri);
    }

    public static String get_github_access_token(String state, String code) {
        final String client_id = store.get_indie_github_client_id();
        final String client_secret = store.get_indie_github_client_secret();

        WSResponse res = store.getwsclient().url("https://github.com/login/oauth/access_token")
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
        return new RandomString(12).nextString();
    }

    public static WSRequest indie_auth_request(WSClient ws, String path) {
        return ws.url("https://api.github.com" + path)
                .setHeader("Authorization", "Basic " + store.get_indie_github_auth())
                .setHeader("Accept", "application/vnd.github.v3 + json");
    }

    public static WSRequest user_auth_request(WSClient ws, String token, String path) {
        return ws.url("https://api.github.com" + path)
                .setHeader("Authorization", "token " + token)
                .setHeader("Accept", "application/vnd.github.v3 + json");
    }

    public static List<repo_model> get_indie_repositories() {
        WSResponse res =  indie_auth_request(store.getwsclient(), "/user/repos")
                .setMethod("GET")
                .execute()
                .get(60, TimeUnit.SECONDS);
        JsonNode json = play.libs.Json.parse(res.getBody());
        ArrayList<repo_model> repos = new ArrayList<>(json.size());
        for (int i = 0; i < json.size(); i++) {
            JsonNode json_repo = json.get(i);
            repos.add(repo_model.from_json(json_repo));
        }
        return repos;
    }

    public static WSRequest post_indie_auth_request(WSClient ws, String path, JsonNode json) {
        return indie_auth_request(ws, "/user/repos")
                .setMethod("POST")
                .setContentType("application/json; charset=utf-8")
                .setBody(json);
    }

    public static repo_model create_new_repo(WSClient ws, String repo_name, String repo_homepage, String repo_description) throws Exception {
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
        WSResponse res = post_indie_auth_request(ws, "/user/repos", json).execute().get(60, TimeUnit.SECONDS);
        if (res.getStatus() == 201) {
            return repo_model.from_name_desc_and_homepage(repo_name, repo_description, repo_homepage);
        }
        throw new Exception(res.getBody());
    }

    public static repo_model get_repo_by_name(String user_name, String repo_name) {
        String path = "/repos/" + user_name + "/" + repo_name;
        WSRequest req = indie_auth_request(WS.client(), path);
        WSResponse res = req.execute().get(60, TimeUnit.SECONDS);
        repo_model repo = repo_model.from_json(play.libs.Json.parse(res.getBody()));
        return repo;
    }

    public static user_model get_user_by_token(String token) {
        WSResponse res_user;
        WSRequest req_user = github_access.user_auth_request(WS.client(), token, "/user")
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        return user_model.from_json(Json.parse(res_user.getBody()));
    }

    public static user_model get_user_by_name(String name) {
        WSResponse res_user;
        WSRequest req_user = github_access.indie_auth_request(WS.client(), "/users/" + name)
                .setMethod("GET");
        F.Promise<WSResponse> pres_user = req_user.execute();
        res_user = pres_user.get(60, TimeUnit.SECONDS);
        return user_model.from_json(Json.parse(res_user.getBody()));
    }
}
