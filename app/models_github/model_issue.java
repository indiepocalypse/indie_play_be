package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_issue {
    public String url;
    public String html_url;
    public String comments_url;
    public Long id;
    public model_user user;
    public String body;
    public int number;
    public int comments;
    public String title;
    // TODO: add created and updated dates, labels, milestone, etc.
    // TODO: use the utils

    public model_issue(
            String p_url,
            String p_html_url,
            String p_comments_url,
            Long p_id,
            model_user p_user,
            String p_body,
            int p_number,
            int p_comments,
            String p_title
    ) {
        this.url = p_url;
        this.html_url = p_html_url;
        this.comments_url = p_comments_url;
        this.id = p_id;
        this.user = p_user;
        this.body = p_body;
        this.number = p_number;
        this.comments = p_comments;
        this.title = p_title;
    }

    public static model_issue from_json(JsonNode json) {
        String url = json.get("url").asText();
        String html_url = json.get("html_url").asText();
        String comments_url = json.get("comments_url").asText();
        Long id = json.get("id").asLong();
        model_user user = model_user.from_json(json.get("user"));
        String body = json.get("body").asText();
        int number = json.get("number").asInt();
        int comments = json.get("comments").asInt();
        String title = json.get("title").asText();
        return new model_issue(
                url,
                html_url,
                comments_url,
                id,
                user,
                body,
                number,
                comments,
                title
        );
    }
}
