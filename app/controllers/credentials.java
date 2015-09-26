package controllers;

import org.jboss.netty.handler.codec.base64.Base64Encoder;
import sun.misc.BASE64Encoder;

import java.io.FileNotFoundException;
import java.util.Base64;

/**
 * Created by skariel on 25/09/15.
 */
public class credentials {
    static final String username = "theindiepocalypse";
    static final String pssw = "lets do it 2";
    String auth = null;

    public credentials() throws FileNotFoundException {
        Base64.Encoder encoder = Base64.getMimeEncoder();
        String str = username+":"+pssw;
        auth = encoder.encodeToString(str.getBytes());
    }
    public String getAuth() {
        return auth;
    }
}
