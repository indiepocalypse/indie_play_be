package models_db_github;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by skariel on 17/10/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_pull_request extends Model {
    private static final Finder<String, model_pull_request> find = new Finder<>(model_pull_request.class);

    @Id
    @Nonnull
    public final String id;
    @Nonnull
    public final String number;
    @Nonnull
    public final String user_name;
    @Nonnull
    public final String SHA;
    @Nonnull
    public final String repo_name;
    @Nonnull
    public final String title;
    @Nonnull
    public final String body;
    @Nonnull
    public final Boolean merged;
    @Nullable
    public final Boolean mergeable; // yeah, might be null per github api. Only while mergeability was not calculated
    @Nonnull
    private final String url;
    @Nonnull
    private final Long github_id;
    @Nonnull
    private final String html_url;
    @Nonnull
    private final String comments_url;
    @Nonnull
    private final String state;

    private model_pull_request(
            @Nonnull String p_url,
            @Nonnull Long p_github_id,
            @Nonnull String p_html_url,
            @Nonnull String p_state,
            @Nonnull String p_title,
            @Nonnull String p_user_name,
            @Nonnull String p_body,
            @Nonnull Boolean p_merged,
            @Nullable Boolean p_mergeable,
            @Nonnull String p_comments_url,
            @Nonnull String p_repo_name,
            @Nonnull String p_number,
            @Nonnull String p_SHA
    ) {
        assert p_url != null;
        assert p_url != null;
        assert p_github_id != null;
        assert p_html_url != null;
        assert p_state != null;
        assert p_title != null;
        assert p_user_name != null;
        assert p_body != null;
        assert p_merged != null;
        assert p_comments_url != null;
        assert p_SHA != null;
        assert p_repo_name != null;
        assert p_number != null;

        this.url = p_url;
        this.github_id = p_github_id;
        this.html_url = p_html_url;
        this.state = p_state;
        this.title = p_title;
        this.user_name = p_user_name;
        this.body = p_body;
        this.merged = p_merged;
        this.mergeable = p_mergeable;
        this.comments_url = p_comments_url;
        this.SHA = p_SHA;
        this.repo_name = p_repo_name;
        this.number = p_number;
        this.id = repo_name + "/" + this.number;
    }

    public static model_pull_request from_webhook_json(@Nonnull JsonNode json) {
        assert json != null;
        String number = Integer.toString(json.get("number").asInt());
        String url = json.get("url").asText();
        Long github_id = json.get("id").asLong();
        String html_url = json.get("html_url").asText();
        String state = json.get("state").asText();
        String title = json.get("title").asText();
        model_user user = model_user.from_json(json.get("user"));
        String body = json.get("body").asText();
        Boolean merged = json.has("merged_at") && json.get("merged_at") != null &&
                json.get("merged_at").asText() != null && !json.get("merged_at").asText().equals("null");
        Boolean mergeable = json.has("mergeable") && json.get("mergeable") != null && json.get("mergeable").asBoolean();
        if ((json.get("mergeable") == null) || (json.get("mergeable").isNull())) {
            mergeable = null;
        }
        String comments_url = json.get("comments_url").asText();
        JsonNode head = json.get("head");
        model_repo repo = model_repo.from_json(json.get("base").get("repo"));
        String SHA = null;
        if (head != null) {
            SHA = head.get("sha").asText();
        }
        return new model_pull_request(
                url,
                github_id,
                html_url,
                state,
                title,
                user.user_name,
                body,
                merged,
                mergeable,
                comments_url,
                repo.repo_name,
                number,
                SHA
        );
    }

    public static Query<model_pull_request> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }

    public model_pull_request same_but_clsoed() {
        final String state = "closed";
        return same_but_with_state_merged_and_mergeable(state, this.merged, this.mergeable);
    }

    public model_pull_request same_but_open() {
        final String state = "open";
        return same_but_with_state_merged_and_mergeable(state, this.merged, this.mergeable);
    }

    public model_pull_request same_but_merged() {
        final String state = "closed";
        final boolean merged = true;
        final boolean mergeable = false;
        return same_but_with_state_merged_and_mergeable(state, merged, mergeable);
    }

    private model_pull_request same_but_with_state_merged_and_mergeable(
            @Nonnull final String p_state,
            final boolean p_merged,
            final boolean p_mergeable) {
        assert p_state != null;
        return new model_pull_request(
                this.url,
                this.github_id,
                this.html_url,
                p_state,
                this.title,
                this.user_name,
                this.body,
                p_merged,
                p_mergeable,
                this.comments_url,
                this.repo_name,
                this.number,
                this.SHA
        );
    }

    public boolean is_closed() {
        return !this.state.equals("open");
    }

}
