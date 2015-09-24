package models;

/**
 * Created by skariel on 22/09/15.
 */

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.HashMap;

@Entity
public class repo extends Model {
    @Id
    public String name;
    public String github_url;
    public int stars;
    public String description;
    public HashMap<String, Float> programming_languages_and_shares;
    public HashMap<String, BigDecimal> owners_and_shares;

    public void sync(String txt) {
        JsonNode all_repos = Json.parse(txt);
        for (int i = 0; i < all_repos.size(); i++) {
            JsonNode repo_json = all_repos.get(i);
            String name = repo_json.get("name").asText();
            repo r = new repo();
            r.name = name;
            r.save();
        }
    }
}
