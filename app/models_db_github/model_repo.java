package models_db_github;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by repo_name")
@Entity
public class model_repo extends Model {
    private static final Finder<String, model_repo> find = new Finder<>(model_repo.class);

    // TODO: notnull annotations!
    @Id
    @Nonnull
    public final String repo_name;
    @Nonnull
    public final String repo_description;
    @Nonnull
    public final String github_html_url;
    @Nonnull
    private final String repo_homepage;
    @Nonnull
    private final Integer stars_count;
    @Nonnull
    private final Integer forks_count;

    private model_repo(
            @Nonnull String repo_name,
            @Nonnull String repo_description,
            @Nonnull String repo_homepage,
            @Nonnull String github_html_url,
            @Nonnull Integer stars_count,
            @Nonnull Integer forks_count) {
        assert repo_name != null;
        assert repo_description != null;
        assert repo_homepage != null;
        assert github_html_url != null;
        assert stars_count != null;
        assert forks_count != null;

        this.repo_name = repo_name;
        this.repo_description = repo_description;
        this.repo_homepage = repo_homepage;
        this.github_html_url = github_html_url;
        this.stars_count = stars_count;
        this.forks_count = forks_count;
    }

    public static model_repo from_json(@Nonnull JsonNode json_repo) {
        assert json_repo != null;
        String name = json_repo.get("name").asText();
        String description = json_repo.get("description").asText();
        String github_html_url = json_repo.get("html_url").asText();
        String homepage = json_repo.get("homepage").asText();
        Integer stars_count = json_repo.get("stargazers_count").asInt();
        Integer forks_count = json_repo.get("forks_count").asInt();
        return new model_repo(name, description, homepage, github_html_url, stars_count, forks_count);
    }

    public static model_repo from_name_desc_and_homepage(@Nonnull String name, @Nonnull String desc, @Nonnull String homepage) {
        assert name != null;
        assert desc != null;
        assert homepage != null;
        return new model_repo(name, desc, homepage, "https://github.com/theindiepocalypse/" + name, 0, 0);
    }

    public static Query<model_repo> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }

}

