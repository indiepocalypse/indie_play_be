package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import models_db_github.model_repo;
import models_db_github.model_user;
import play.Logger;
import scala.Int;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.List;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by file_name")
@Entity
public class model_repo_image extends Model {
    // TODO: split into image model and repo pointer to it
    // TODO: maybe use a repo extended info... like for users
    private static final Finder<String, model_repo_image> find = new Finder<>(model_repo_image.class);
    @Id
    public final String file_name;
    public final String repo_name;
    @Lob
    private final byte[] image;
    public final Date uploaded_date;
    public final String uploaded_by_user_name;

    public model_repo_image(String p_repo_name, String p_user_name, byte[] p_image) {
        this.uploaded_by_user_name = p_user_name;
        this.repo_name = p_repo_name;
        this.image = p_image;
        this.uploaded_date = new Date();
        this.file_name = new utils.utils_random_string(12).nextString();
        Logger.info("FILE NAME: "+this.file_name);
        Logger.info("MODEL_USER_NAME: "+p_user_name);
    }

    public static Query<model_repo_image> fetch() {
        return find.setUseQueryCache(true);
    }

    public byte[] getImage() {
        return this.image;
    }

}
