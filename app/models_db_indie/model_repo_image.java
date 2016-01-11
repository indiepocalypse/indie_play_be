package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;

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
    public final String id;
    @Nonnull
    public final String unique_file_name; // this is the image id
    @Nonnull
    public final String repo_name;

    public model_repo_image(
            @Nonnull String p_repo_name,
            @Nonnull String p_unique_file_name) {
        assert p_repo_name != null;
        assert p_unique_file_name != null;

        this.id = "repo_image_for_repo_"+p_repo_name+"_unique_file_name_"+p_unique_file_name;
        this.repo_name = p_repo_name;
        this.unique_file_name = p_unique_file_name;
    }

    public
    @Nonnull
    static Query<model_repo_image> fetch() {
        return find.setUseQueryCache(true);
    }

    public static void deleteById(@Nonnull String id) {
        assert id != null;
        find.deleteById(id);
    }
}
