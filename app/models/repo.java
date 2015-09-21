package models;

/**
 * Created by skariel on 22/09/15.
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import com.avaje.ebean.Model;

@Entity
public class repo {
    @Id
    public String id;
    public String name;
}
