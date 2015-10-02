package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Created by skariel on 25/09/15.
 */
public class credentials {
    private String auth = null;
    private String client_id = null;
    private String client_secret = null;
    public String name = null;
    public String pssw = null;

    public credentials() {
        String tmp_name = ConfigFactory.load().getString("credentials.indie.github.username");
        String tmp_pssw = ConfigFactory.load().getString("credentials.indie.github.pssw");
        client_id = ConfigFactory.load().getString("credentials.indie.github.client_id");
        client_secret = ConfigFactory.load().getString("credentials.indie.github.client_secret");
        try {
            JsonNode json = play.libs.Json.parse(new FileInputStream("app/controllers/.github_indie_credentials_local_secret"));
            tmp_name = json.get("username").asText();
            tmp_pssw = json.get("pssw").asText();
            client_id = json.get("client_id").asText();
            client_secret = json.get("client_secret").asText();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        name = tmp_name;
        pssw = tmp_pssw;
        InputStream f = getClass().getResourceAsStream(".github_indie_credentials_local_secret");
        Base64.Encoder encoder = Base64.getMimeEncoder();
        String str = tmp_name+":"+tmp_pssw;
        auth = encoder.encodeToString(str.getBytes());
    }
    public String getAuth() {
        return auth;
    }
    public String getClient_id() {
        return client_id;
    }
    public String getClient_secret() {
        return client_secret;
    }
}
