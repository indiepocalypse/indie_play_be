package models;

/**
 * Created by skariel on 22/09/15.
 */

import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class user {
    @Id
    public String name;
    public String email;
    public Formats.DateTime joined_datetime;
}
