package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by skariel on 25/09/15.
 */
public class credentials {
    String username = null;
    String pssw = null;

    public credentials() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("c://filename");
        JsonNode credentials = Json.parse(fis);
        username = credentials.get("user").asText();
        pssw = credentials.get("psw").asText();
    }
    public java.lang.String getUsername() {
        return username;
    }
    public String getPssw() {
        return pssw;
    }
}
