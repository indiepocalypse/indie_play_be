package models;

/**
 * Created by skariel on 22/09/15.
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
public class user {
    @Id
    public String username;
    public String email;
    public ArrayList<String> repos;
}
