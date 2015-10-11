package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_gmail_last_date_read extends Model {
    public static final String constid = "gmail_last_message_date_read_id_1789627853";
    public static Finder<String, model_gmail_last_date_read> find = new Finder<String, model_gmail_last_date_read>(model_gmail_last_date_read.class);
    @Id
    public String id;
    public Date lastdate;

    public model_gmail_last_date_read(Date date) {
        this.lastdate = date;
        this.id = constid;
    }

}

