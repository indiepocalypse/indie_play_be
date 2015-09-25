package controllers;

import java.io.FileNotFoundException;
import java.util.Base64;

/**
 * Created by skariel on 25/09/15.
 */
public class credentials {
    static final String username = "theindiepocalypse";
    static final String pssw = "its just good 1";
    String auth = null;

    public credentials() throws FileNotFoundException {
        Base64.Encoder encoder = Base64.getMimeEncoder();
        String str = username+":"+pssw;
        auth = encoder.encode(str.getBytes()).toString();
    }
    public String getAuth() {
        return auth;
    }
}
