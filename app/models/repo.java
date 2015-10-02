package models;

/**
 * Created by skariel on 22/09/15.
 */

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.HashMap;

@Entity
public class repo extends Model {
    @Id
    public String repoame;
    public String github_url;
    public int stars;
    public String description;
    public HashMap<String, Float> programming_languages_and_shares;
    public HashMap<String, BigDecimal> owners_and_shares;
    public static Finder<String, repo> find = new Finder<String,repo>(repo.class);
}
