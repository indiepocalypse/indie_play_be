package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_comment {
    public final String url;
    public final String html_url;
    public final String issue_url;
    public final Long id;
    public final model_user user;
    public final String body;

    public model_comment(
            String p_url,
            String p_html_url,
            String p_issue_url,
            Long p_id,
            model_user p_user,
            String p_body
    ) {
        this.url = p_url;
        this.html_url = p_html_url;
        this.issue_url = p_issue_url;
        this.id = p_id;
        this.user = p_user;
        this.body = p_body;
    }

    public static model_comment from_json(JsonNode json) {
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
