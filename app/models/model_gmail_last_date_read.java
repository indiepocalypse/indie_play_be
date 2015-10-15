package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.annotation.EntityConcurrencyMode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */

@Entity
public class model_gmail_last_date_read extends Model {
    private static final String constid = "gmail_last_message_date_read_id_1789627853";
    public static final Finder<String, model_gmail_last_date_read> find = new Finder<>(model_gmail_last_date_read.class);
    @Id
    public final String id;
    public Date lastdate;

    public model_gmail_last_date_read(Date date) {
        this.lastdate = date;
        this.id = constid;
    }

    public static model_gmail_last_date_read get_a_copy_of_the_singleton() {
        model_gmail_last_date_read last_date_read_model = null;
        try {
            last_date_read_model = model_gmail_last_date_read.find.byId(model_gmail_last_date_read.constid);
        }
        catch (Exception ignored) {
        }
        return last_date_read_model;
    }
}

