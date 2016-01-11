package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.CacheStrategy;
import play.Logger;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by unique_file_name")
@Entity
public class model_image extends Model {
    @Nonnull
    private static final Finder<String, model_image> find = new Finder<>(model_image.class);
    @Id
    @Nonnull
    public final String unique_file_name;
    @Nonnull
    private final Date uploaded_date;
    @Nonnull
    private final String uploaded_by_user_name;
    @Lob
    @Nonnull
    private final byte[] image;

    public model_image(
            @Nonnull String p_user_name,
            @Nonnull byte[] p_image,
            @Nonnull Date p_uploaded_date,
            @Nonnull String p_unique_file_name) {
        assert p_user_name != null;
        assert p_image != null;
        assert p_uploaded_date != null;
        assert p_unique_file_name != null;

        this.uploaded_by_user_name = p_user_name;
        this.image = p_image;
        this.uploaded_date = p_uploaded_date;
        this.unique_file_name = p_unique_file_name;
        Logger.info("FILE NAME: " + this.unique_file_name);
        Logger.info("MODEL_USER_NAME: " + p_user_name);
    }

    public model_image(
            @Nonnull String p_user_name,
            @Nonnull byte[] p_image) {
        this(p_user_name, p_image,
                new Date(),                                         // Date uploaded
                new utils.utils_random_string(12).nextString()      // unique file name
        );
    }

    public static model_image from_sqlrow(@Nonnull SqlRow row) {
        assert row != null;
        return new model_image(
                row.getString("user_name"),
                (byte[])row.get("image"),
                row.getDate("uploaded_date"),
                row.getString("unique_file_name")
        );
    }

    public
    @Nonnull
    static Query<model_image> fetch() {
        return find.setUseQueryCache(true);
    }

    public
    @Nonnull
    byte[] getImage() {
        return this.image;
    }

}
