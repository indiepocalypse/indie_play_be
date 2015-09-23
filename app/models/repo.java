package models;

/**
 * Created by skariel on 22/09/15.
 */

import com.avaje.ebean.Model;

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
    public HashMap<String, Float> programming_languages;
    public HashMap<String, BigDecimal> owners;
}
