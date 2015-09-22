package models;

/**
 * Created by skariel on 22/09/15.
 */

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class repo {
    // TODO: rename to github_reopo
    // TODO: create an indie_repo

    @Id
    public String id;
    public String name;
}
