package stores;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import play.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Base64;

/**
 * Created by skariel on 11/10/15.
 */
public class store_credentials {
    public static final credentials_github github = new credentials_github();
    public static final credentials_gmail gmail = new credentials_gmail();

    public static class credentials_github {
        public String name = null;
        public String pssw = null;
        private String auth = null;
        private String client_id = null;
        private String client_secret = null;

        public credentials_github() {
            String tmp_name = ConfigFactory.load().getString("credentials.indie.github.username");
            String tmp_pssw = ConfigFactory.load().getString("credentials.indie.github.pssw");
            client_id = ConfigFactory.load().getString("credentials.indie.github.client_id");
            client_secret = ConfigFactory.load().getString("credentials.indie.github.client_secret");
            try {
                JsonNode json = play.libs.Json.parse(new FileInputStream("app/stores/.local_secret_github_indie_credentials"));
                tmp_name = json.get("username").asText();
                tmp_pssw = json.get("pssw").asText();
                client_id = json.get("client_id").asText();
                client_secret = json.get("client_secret").asText();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            name = tmp_name;
            pssw = tmp_pssw;
            Base64.Encoder encoder = Base64.getMimeEncoder();
            String str = tmp_name + ":" + tmp_pssw;
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

    public static class credentials_gmail {
        public String name = null;
        public String pssw = null;

        public credentials_gmail() {
            String tmp_name = ConfigFactory.load().getString("credentials.indie.gmail.username");
            String tmp_pssw = ConfigFactory.load().getString("credentials.indie.gmail.pssw");
            try {
                JsonNode json = play.libs.Json.parse(new FileInputStream("app/stores/.local_secret_gmail_indie_credentials"));
                tmp_name = json.get("username").asText();
                tmp_pssw = json.get("pssw").asText();
            } catch (FileNotFoundException e) {
                Logger.warn("while loading gmail credentials... ", e);
            }
            name = tmp_name;
            pssw = tmp_pssw;
        }

    }
}
