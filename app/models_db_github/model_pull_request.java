package models_db_github;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by skariel on 17/10/15.
 */
@Entity
public class model_pull_request extends Model {
    public static final Finder<String, model_pull_request> find = new Finder<>(model_pull_request.class);

    @Id
    public final String id;
    public final String url;
    public final Long github_id;
    public final String html_url;
    public final String number;
    public String state;
    public String title;
    @ManyToOne
    public final model_user user;
    public String body;
    public Boolean merged;
    public Boolean mergeable;
    public final String comments_url;
    public final String SHA;
    @ManyToOne
    public final model_repo repo;

    public model_pull_request(
            String p_url,
            Long p_github_id,
            String p_html_url,
            String p_state,
            String p_title,
            model_user p_user,
            String p_body,
            Boolean p_merged,
            Boolean p_mergeable,
            String p_comments_url,
            model_repo p_repo,
            String p_number,
            String p_SHA
    ) {
        this.url = p_url;
        this.github_id = p_github_id;
        this.html_url = p_html_url;
        this.state = p_state;
        this.title = p_title;
        this.user = p_user;
        this.body = p_body;
        this.merged = p_merged;
        this.mergeable = p_mergeable;
        this.comments_url = p_comments_url;
        this.SHA = p_SHA;
        this.repo = p_repo;
        this.number = p_number;
        this.id = repo.repo_name + "/" + this.number;
    }

    public boolean is_closed() {
        return !this.state.equals("open");
    }

    public static model_pull_request from_json(JsonNode json) {
        // TODO: can an int json value be parsed as string? the way below seems... just too much
        String number = Integer.toString(json.get("number").asInt());
        String url = json.get("url").asText();
        Long github_id = json.get("id").asLong();
        String html_url = json.get("html_url").asText();
        String state = json.get("state").asText();
        String title = json.get("title").asText();
        model_user user = model_user.from_json(json.get("user"));
        String body = json.get("body").asText();
        Boolean merged = json.has("merged_at") && json.get("merged_at")!=null &&
                json.get("merged_at").asText()!=null && !json.get("merged_at").asText().equals("null");
        Boolean mergeable = json.has("mergeable") && json.get("mergeable")!=null && json.get("mergeable").asBoolean();
        if ((json.get("mergeable")==null) || (json.get("mergeable").isNull())) {
            mergeable = null;
        }
        String comments_url = json.get("comments_url").asText();
        JsonNode head = json.get("head");
        model_repo repo = model_repo.from_json(json.get("base").get("repo"));
        String SHA = null;
        if (head!=null) {
            SHA = head.get("sha").asText();
        }
        return new model_pull_request(
            url,
            github_id,
            html_url,
            state,
            title,
            user,
            body,
            merged,
            mergeable,
            comments_url,
            repo,
            number,
            SHA
        );
    }

}
