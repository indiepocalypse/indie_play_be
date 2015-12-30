package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
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
@CacheStrategy(readOnly = true, warmingQuery = "order by file_name")
@Entity
public class model_repo_image extends Model {
    // TODO: split into image model and repo pointer to it
    // TODO: maybe use a repo extended info... like for users
    @Nonnull
    private static final Finder<String, model_repo_image> find = new Finder<>(model_repo_image.class);
    @Id
    @Nonnull
    public final String file_name;
    @Nonnull
    private final String repo_name;
    @Nonnull
    private final Date uploaded_date;
    @Nonnull
    private final String uploaded_by_user_name;
    @Lob
    @Nonnull
    private final byte[] image;

    public model_repo_image(
            @Nonnull String p_repo_name,
            @Nonnull String p_user_name,
            @Nonnull byte[] p_image) {
        assert p_user_name != null;
        assert p_repo_name != null;
        assert p_image != null;

        this.uploaded_by_user_name = p_user_name;
        this.repo_name = p_repo_name;
        this.image = p_image;
        this.uploaded_date = new Date();
        this.file_name = new utils.utils_random_string(12).nextString();
        Logger.info("FILE NAME: " + this.file_name);
        Logger.info("MODEL_USER_NAME: " + p_user_name);
    }

    public
    @Nonnull
    static Query<model_repo_image> fetch() {
        return find.setUseQueryCache(true);
    }

    public
    @Nonnull
    byte[] getImage() {
        return this.image;
    }

}
