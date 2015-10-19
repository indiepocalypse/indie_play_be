package models_github;

import com.fasterxml.jackson.databind.JsonNode;
import models.model_user;

/**
 * Created by skariel on 17/10/15.
 */
public class model_pull_request {
    public final String url;
    public final Long id;
    public final String html_url;
    public final int number;
    public final String state;
    public final String title;
    public final model_user user;
    public final String body;
    public final boolean merged;
    public final boolean mergeable;
    public final String comments_url;
    public final int comments;
    public final int additions;
    public final int deletions;
    public final int changed_files;
    public final String SHA;
    // TODO: add many missing fields

    public model_pull_request(
            String p_url,
            Long p_id,
            String p_html_url,
            Integer p_number,
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
        this.id = p_id;
        this.html_url = p_html_url;
        this.number = p_number;
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
    }

    public static model_pull_request from_json(JsonNode json) {
        String url = utils.utils_json.str_or_null(json, "url");
        Long id = utils.utils_json.long_or_null(json, "id");
        String html_url = utils.utils_json.str_or_null(json, "html_url");
        Integer number = utils.utils_json.int_or_null(json, "number");
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
        String SHA = head.get("label").asText() + "@" + head.get("sha");
        return new model_pull_request(
            url,
            id,
            html_url,
            number,
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
