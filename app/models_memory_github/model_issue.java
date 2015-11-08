package models_memory_github;

import com.fasterxml.jackson.databind.JsonNode;
import models_db_github.model_user;

/**
 * Created by skariel on 14/10/15.
 */
public class model_issue {
    // TODO: add labels, etc.
    public final String url;
    public final String html_url;
    public final String comments_url;
    public final Long id;
    public final model_user user;
    public final String number;
    public final int comments;
    public String body;
    public String title;
    public String state;

    public model_issue(
            String p_url,
            String p_html_url,
            String p_comments_url,
            Long p_id,
            model_user p_user,
            String p_body,
            String p_number,
            int p_comments,
            String p_title,
            String p_state
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
        this.state = p_state;
    }

    public static model_issue from_json(JsonNode json) {
        String url = json.get("url").asText();
        String html_url = json.get("html_url").asText();
        String comments_url = json.get("comments_url").asText();
        Long id = json.get("id").asLong();
        model_user user = model_user.from_json(json.get("user"));
        String body = json.get("body").asText();
        // TODO: parsing a number as string, allowed? below seems just... too much
        String number = Integer.toString(json.get("number").asInt());
        int comments = json.get("comments").asInt();
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
