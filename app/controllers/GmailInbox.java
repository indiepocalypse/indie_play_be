package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.mail.imap.IMAPFolder;
import com.typesafe.config.ConfigFactory;
import play.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Folder;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import models.gmail_last_date_read;

public class GmailInbox {
    // TODO: se this for using idle: http://stackoverflow.com/questions/4155412/javamail-keeping-imapfolder-idle-alive
    static IMAPFolder inbox = null;
    static Store store = null;
    static final long delta_milis_reload = 60*1000*17; // 17 minutes
    static final String gmail_last_date_read_id = "gmail_last_date_read_id";
    public static int mail_count = 0;

    public static void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(delta_milis_reload);
                    reload_folder();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        reload_folder();
        new Thread(() -> {
            while (true) {
                // TODO: accept repo transfer...
                try {
                    if (inbox!=null) {
                        inbox.idle(true);
                    }
                } catch (Exception e) {
                    Logger.error("Error in gmail_inbox!",e);
                }
            }
        }).start();
    }


    public static void handle_messages(Message[] ms) {
        try {


            gmail_last_date_read last_date_read_model = gmail_last_date_read.find.byId(gmail_last_date_read_id);
            if (last_date_read_model==null) {
                //
            }

        }
        catch (Exception e) {
            Logger.error("while handling gmail message... ", e);
        }
    }


    public static void reload_folder() {
        try {
            if (inbox != null) {
                inbox.close(true);
            }
            if (store != null) {
                store.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String tmp_name = ConfigFactory.load().getString("credentials.indie.gmail.username");
        String tmp_pssw = ConfigFactory.load().getString("credentials.indie.gmail.pssw");

        try {
            JsonNode json = play.libs.Json.parse(new FileInputStream("app/controllers/.gmail_indie_credentials_local_secret"));
            tmp_name = json.get("username").asText();
            tmp_pssw = json.get("pssw").asText();
        }
        catch (FileNotFoundException e) {
            Logger.warn("While loading gmail credentials... ", e);
        }

        final Properties properties = System.getProperties();
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.imap.socketFactory.fallback", "false");

        properties.setProperty("mail.imap.host", "imap.gmail.com");
        properties.setProperty("mail.imap.port", "993");
        properties.setProperty("mail.imap.connectiontimeout", "5000");
        properties.setProperty("mail.imap.timeout", "5000");

        Session imap_session = Session.getDefaultInstance(properties, null);
        imap_session.setDebug(false);

        try {
            store = imap_session.getStore("imaps");
            store.connect("imap.gmail.com", tmp_name, tmp_pssw);
            inbox = (IMAPFolder) store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            mail_count = inbox.getMessageCount();
            inbox.addMessageCountListener(new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent messageCountEvent) {
                    mail_count += 1;
                    for (Message m: messageCountEvent.getMessages()) {
                        handle_message(m);
                    }
                }

                @Override
                public void messagesRemoved(MessageCountEvent messageCountEvent) {
                    mail_count -= 1;
                }
            });
        }
        catch (Exception e) {
            Logger.error("While reloading gmail inbox... ", e);
        }
    }
}