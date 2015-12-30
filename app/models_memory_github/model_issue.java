package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_user;

import javax.annotation.Nonnull;

/**
 * Created by skariel on 14/10/15.
 */
public class model_issue {
    @Nonnull
    public final model_user user;
    @Nonnull
    public final String number;
    @Nonnull
    public final String body;
    @Nonnull
    public final String title;
    // TODO: add labels, etc.
    @Nonnull
    private final String url;
    @Nonnull
    private final String html_url;
    @Nonnull
    private final String comments_url;
    @Nonnull
    private final Long id;
    @Nonnull
    private final String comments;
    @Nonnull
    public String state;

    private model_issue(
            @Nonnull String p_url,
            @Nonnull String p_html_url,
            @Nonnull String p_comments_url,
            @Nonnull Long p_id,
            @Nonnull model_user p_user,
            @Nonnull String p_body,
            @Nonnull String p_number,
            @Nonnull String p_comments,
            @Nonnull String p_title,
            @Nonnull String p_state
    ) {
        assert p_url != null;
        assert p_html_url != null;
        assert p_comments_url != null;
        assert p_id != null;
        assert p_user != null;
        assert p_body != null;
        assert p_number != null;
        assert p_comments != null;
        assert p_title != null;
        assert p_state != null;

        this.url = p_url;
        this.html_url = p_html_url;
        this.comments_url = p_comments_url;
        this.id = p_id;
        this.user = p_user;
        this.body = p_body;
        this.number = p_number;
        this.comments = p_comments;
        this.title = p_title;
        this.state = p_state;
    }

    public static model_issue from_json(@Nonnull JsonNode json) {
        assert json != null;
        String url = json.get("url").asText();
        String html_url = json.get("html_url").asText();
        String comments_url = json.get("comments_url").asText();
        Long id = json.get("id").asLong();
        model_user user = model_user.from_json(json.get("user"));
        String body = json.get("body").asText();
        String number = Integer.toString(json.get("number").asInt());
        String comments = json.get("comments").asText();
        String title = json.get("title").asText();
        String state = json.get("state").asText();
        return new model_issue(
                url,
                html_url,
                comments_url,
                id,
                user,
                body,
                number,
                comments,
                title,
                state
        );
    }

    public boolean is_closed() {
        return !this.state.equals("open");
    }
}
