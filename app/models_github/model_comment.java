package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_user;

import java.util.Date;

/**
 * Created by skariel on 14/10/15.
 */
public class model_comment {
    public String url;
    public String html_url;
    public String issue_url;
    public Long id;
    public model_user user;
    public String body;
    // TODO: add created and updated dates

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
        String issue_url = json.get("comments_url").asText();
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