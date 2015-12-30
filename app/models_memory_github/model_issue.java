package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_issue {
    public final model_user user;
    public final String number;
    public final String body;
    public final String title;
    // TODO: add labels, etc.
    private final String url;
    private final String html_url;
    private final String comments_url;
    private final Long id;
    private final String comments;
    public String state;

    private model_issue(
            String p_url,
            String p_html_url,
            String p_comments_url,
            Long p_id,
            model_user p_user,
            String p_body,
            String p_number,
            String p_comments,
            String p_title,
            String p_state
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

    public static model_issue from_json(JsonNode json) {
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
