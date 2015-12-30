package models_db_github;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.CacheStrategy;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by user_name")
@Entity
public class model_user extends Model {
    private static final Finder<String, model_user> find = new Finder<>(model_user.class);

    @Id
    @Nonnull
    public final String user_name;
    @Nonnull
    public final String github_html_url;
    @Nonnull
    public final String avatar_url;

    public model_user(
            @Nonnull String p_user_name,
            @Nonnull String p_github_html_url,
            @Nonnull String p_avatar_url) {
        assert p_user_name != null;
        assert p_github_html_url != null;
        assert p_avatar_url != null;

        this.user_name = p_user_name;
        this.github_html_url = p_github_html_url;
        this.avatar_url = p_avatar_url;
    }

    public static model_user from_json(@Nonnull JsonNode json_user) {
        assert json_user != null;
        String user_avatar_url = json_user.get("avatar_url").asText();
        String user_name = json_user.get("login").asText();
        String user_github_html_url = json_user.get("html_url").asText();
        return new model_user(
                user_name, user_github_html_url, user_avatar_url);
    }

    public static Query<model_user> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }

    public static model_user from_sqlrow(@Nonnull SqlRow row) {
        assert row != null;
        // TODO: there must be an automatic way to do this...
        return new model_user(
                row.getString("user_name"),
                row.getString("github_html_url"),
                row.getString("avatar_url")
        );
    }
}

