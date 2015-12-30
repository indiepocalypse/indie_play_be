package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_user;

import javax.annotation.Nonnull;

/**
 * Created by skariel on 14/10/15.
 */
public class model_comment {
    @Nonnull
    public final String body;
    @Nonnull
    private final String url;
    @Nonnull
    private final String html_url;
    @Nonnull
    private final String issue_url;
    @Nonnull
    private final Long id;
    @Nonnull
    private final model_user user;

    private model_comment(
            @Nonnull String p_url,
            @Nonnull String p_html_url,
            @Nonnull String p_issue_url,
            @Nonnull Long p_id,
            @Nonnull model_user p_user,
            @Nonnull String p_body
    ) {
        assert p_url != null;
        assert p_html_url != null;
        assert p_issue_url != null;
        assert p_id != null;
        assert p_user != null;
        assert p_body != null;

        this.url = p_url;
        this.html_url = p_html_url;
        this.issue_url = p_issue_url;
        this.id = p_id;
        this.user = p_user;
        this.body = p_body;
    }

    public static model_comment from_json(@Nonnull JsonNode json) {
        assert json != null;
        String url = json.get("url").asText();
        String html_url = json.get("html_url").asText();
        String issue_url = json.get("issue_url").asText();
        Long id = json.get("id").asLong();
        model_user user = model_user.from_json(json.get("user"));
        String body = json.get("body").asText();
        return new model_comment(
                url,
                html_url,
                issue_url,
                id,
                user,
                body
        );
    }
}
