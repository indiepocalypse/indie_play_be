package models;

import com.avaje.ebean.Model;

import javax.persistence.*;

import com.fasterxml.jackson.databind.JsonNode;

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
    public final Integer number;
    public final String state;
    public final String title;
    public final model_user user;
    public final String body;
    public final Boolean merged;
    public final Boolean mergeable;
    public final String comments_url;
    public final Integer comments;
    public final Integer additions;
    public final Integer deletions;
    public final Integer changed_files;
    public final String SHA;
    public final String repo_name;
    // TODO: add many missing fields

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
            Integer p_comments,
            Integer p_additions,
            Integer p_deletions,
            Integer p_changed_files,
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
        this.comments = p_comments;
        this.additions = p_additions;
        this.deletions = p_deletions;
        this.changed_files = p_changed_files;
        this.SHA = p_SHA;
        String[] surl = url.split("/");
        this.repo_name = surl[5];
        this.number = Integer.parseInt(surl[7]);
        this.id = this.repo_name + "/" + Integer.toString(this.number);
    }

    public static model_pull_request from_json(JsonNode json) {
        String url = utils.utils_json.str_or_null(json, "url");
        Long github_id = utils.utils_json.long_or_null(json, "id");
        String html_url = utils.utils_json.str_or_null(json, "html_url");
        String state = utils.utils_json.str_or_null(json, "state");
        String title = utils.utils_json.str_or_null(json, "title");
        model_user user = model_user.from_json(json.get("user"));
        String body = utils.utils_json.str_or_null(json, "body");
        Boolean merged = utils.utils_json.false_otherwise(json, "merged");
        Boolean mergeable = utils.utils_json.false_otherwise(json, "mergeable");
        String comments_url = utils.utils_json.str_or_null(json, "comments_url");
        Integer comments = utils.utils_json.int_or_null(json, "comments");
        Integer additions = utils.utils_json.int_or_null(json, "additions");
        Integer deletions = utils.utils_json.int_or_null(json, "deletions");
        Integer changed_files = utils.utils_json.int_or_null(json, "changed_files");
        JsonNode head = json.get("head");
        String SHA = null;
        if (head!=null) {
            SHA = head.get("label").asText() + "@" + head.get("sha");
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
            comments,
            additions,
            deletions,
            changed_files,
            SHA
        );
    }

}
