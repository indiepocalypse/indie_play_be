package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import play.Logger;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_gmail_last_date_read extends Model {
    private static final Finder<String, model_gmail_last_date_read> find = new Finder<>(model_gmail_last_date_read.class);

    private static final String constid = "gmail_last_message_date_read_id_1789627853";
    @Id
    private final String id;
    public Date lastdate;

    public model_gmail_last_date_read(@Nonnull Date date) {
        assert date != null;
        this.lastdate = date;
        this.id = constid;
    }

    public static model_gmail_last_date_read get_a_copy_of_the_singleton() {
        model_gmail_last_date_read last_date_read_model = null;
        try {
            last_date_read_model = model_gmail_last_date_read.fetch().findUnique();
        } catch (Exception ignored) {
            Logger.info("last_date_model not found in DB");
        }
        return last_date_read_model;
    }

    private static Query<model_gmail_last_date_read> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(String id) {
        find.deleteById(id);
    }

}

