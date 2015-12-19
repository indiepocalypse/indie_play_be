package models_db_github;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by user_name")
@Entity
public class model_user extends Model {
    static final Finder<String, model_user> find = new Finder<>(model_user.class);

    @Id
    public final String user_name;
    public final String github_html_url;
    public final String avatar_url;

    public model_user(String p_user_name, String p_github_html_url, String p_avatar_url) {
        this.user_name = p_user_name;
        this.github_html_url = p_github_html_url;
        this.avatar_url = p_avatar_url;
    }

    public static model_user from_json(JsonNode json_user) {
        String user_avatar_url = json_user.get("avatar_url").asText();
        String user_name = json_user.get("login").asText();
        String user_github_html_url = json_user.get("html_url").asText();
        return new model_user(
                user_name, user_github_html_url, user_avatar_url);
    }

    public static Query<model_user> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

