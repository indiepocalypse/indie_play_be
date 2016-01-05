package stores;

import com.typesafe.config.ConfigFactory;

import java.util.Base64;

/**
 * Created by skariel on 11/10/15.
 */
public class store_credentials {

    public static String get_github_indie_user_name() {
        return ConfigFactory.load().getString("credentials.indie.github.username");
    }
    public static String get_github_indie_password() {
        return ConfigFactory.load().getString("credentials.indie.github.pssw");
    }
    public static String get_github_indie_client_id() {
        return ConfigFactory.load().getString("credentials.indie.github.client_id");
    }
    public static String get_github_indie_client_secret() {
        return ConfigFactory.load().getString("credentials.indie.github.client_secret");
    }
    public static String get_github_indie_auth() {
        Base64.Encoder encoder = Base64.getMimeEncoder();
        String str = get_github_indie_user_name() + ":" + get_github_indie_password();
        return encoder.encodeToString(str.getBytes());
    }


    public static String get_github_test_user_name() {
        return ConfigFactory.load().getString("credentials.test_user.github.username");
    }
    public static String get_github_test_user_password() {
        return ConfigFactory.load().getString("credentials.test_user.github.pssw");
    }


    public static String get_gmail_indie_user_name() {
        return ConfigFactory.load().getString("credentials.indie.gmail.username");
    }
    public static String get_gmail_indie_password() {
        return ConfigFactory.load().getString("credentials.indie.gmail.pssw");
    }
}
