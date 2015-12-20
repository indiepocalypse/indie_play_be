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
    private static final Finder<String, model_repo_image> find = new Finder<>(model_repo_image.class);
    @ManyToOne
    public final model_repo repo;
    @Lob
    public final byte[] image;
    public final Date uploaded_date;
    public final model_user uploaded_by_user;
    @Id
    public final String file_name;

    public model_repo_image(model_repo p_repo, model_user p_user, byte[] p_image, String file_name) {
        this.uploaded_by_user = p_user;
        this.repo = p_repo;
        this.image = p_image;
        this.uploaded_date = new Date();
        // we assume length is larger than 3
        final String last_three = file_name.substring(file_name.length()-3);
        this.file_name = "image"+new utils.utils_random_string(12).nextString()+"."+last_three;
        Logger.info("FILE NAME: "+this.file_name);
    }

    public static Query<model_repo_image> fetch() {
        return find.setUseQueryCache(true)
                .fetch("repo")
                .fetch("uploaded_by_user");
    }

}
