package models;

/**
 * Created by skariel on 22/09/15.
 */

import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
public class user {
    @Id
    public String name;
    public String email;
    public Formats.DateTime joined_datetime;
    public Formats.DateTime updated_datetime;
    public ArrayList<String> repos;
}