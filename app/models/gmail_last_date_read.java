package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class gmail_last_date_read  extends Model {
    @Id
    public String id;
    public Date date;
    public static Finder<String, gmail_last_date_read> find = new Finder<String,gmail_last_date_read>(gmail_last_date_read.class);

    public static final String constid = "gmail_last_message_date_read_id_1789627853";

    public gmail_last_date_read(Date date) {
        this.date = date;
        this.id = constid;
    }

}
