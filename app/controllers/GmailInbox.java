package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.mail.imap.IMAPFolder;
import com.typesafe.config.ConfigFactory;
import play.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Folder;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;

import models.gmail_last_date_read;
import play.libs.ws.WSResponse;

public class GmailInbox {
    // TODO: se this for using idle: http://stackoverflow.com/questions/4155412/javamail-keeping-imapfolder-idle-alive
    static IMAPFolder inbox = null;
    static Store store = null;
    static final long delta_milis_reload = 60*1000*17; // 17 minutes
    static Thread t1 = null;
    static Thread t2 = null;
    public static int mail_count = 0;

    public static void start() {
        t1 = new Thread() {
            public void run() {
                while (!interrupted()) {
                    try {
                        Thread.sleep(delta_milis_reload);
                        reload_folder();
                    } catch (Exception e) {
                        Logger.error("while sleeping to reload inbox...", e);
                    }
                }
            }
        };
        t1.start();

        t2 = new Thread() {
            public void run() {
                reload_folder();
                while (!interrupted()) {
                    // TODO: accept repo transfer...
                    try {
                        Thread.sleep(50);
                        if (inbox != null) {
                            inbox.idle(true);
                        }
                    } catch (Exception e) {
                        Logger.error("Error in gmail idle...", e);
                    }
                }
            }
        };
        t2.start();
    }

    public static void stop() {
        if (t1!=null) {
            t1.interrupt();
            t1 = null;
        }
        if (inbox!=null) {
            try {
                inbox.close(true);
            }
            catch (Exception e) {
                Logger.error("while closing inbox...", e);
            }
            inbox = null;
        }
        if (t2!=null) {
            t2.interrupt();
            t2 = null;
        }
        if (store!=null) {
            try {
                store.close();
            }
            catch (Exception e) {
                Logger.error("while closing inbox...", e);
            }
            store = null;
        }
    }


    public static void handle_messages(Message[] ms) {
        gmail_last_date_read last_date_read_model = null;
        try {
            last_date_read_model = gmail_last_date_read.find.byId(gmail_last_date_read.constid);
        }
        catch (Exception ignored) {
        }
        Logger.info("#messages="+ Integer.toString(ms.length));
        for (Message m: ms) {
            Date m_date = null;
            String m_subject = null;
            String m_body = null;
            String m_from = null;
            try {
                m_date = m.getReceivedDate();
                m_subject = m.getSubject();
                //m_body = mime.getBodyPart(0).getContent().toString();
                try {
                    m_body = (String)m.getContent();
                }
                catch (Exception ignored_this_one_too) {
                    m_body = "just something";
                }
                m_from = m.getFrom()[0].toString();
            }
            catch (Exception e) {
                Logger.error("while reading message date...", e);
            }
            if ((m_date==null)||(m_subject==null)||(m_body==null)) {
                continue;
            }
            if (last_date_read_model == null) {
                last_date_read_model = new gmail_last_date_read(m_date);
            }
            else {
                if (last_date_read_model.lastdate.before(m_date)) {
                    last_date_read_model.lastdate = m_date;
                }
            }

            // TODO: actually handle the message....
            if ((m_from!=null)&&(m_from.equals("GitHub <support@github.com>"))) {
                if ((m_subject!=null)&&(m_subject.contains("Repository transfer from"))) {
                    if (m_body!=null) {
                        String lines[] = m_body.split("\\r?\\n");
                        for (String l: lines) {
                            String lt = l.trim();
                            if (lt.startsWith("https")) {
                                // accept the repo!
                                Logger.info("The repo transfer url: "+lt);
                                github_iojs.accept_trasfer_repo(lt);
                            }
                        }
                    }
                }
            }

            Logger.info("handling message with title: "+m_subject);
            //Logger.info("and body:"+m_body);
            Logger.info("From:"+m_from);
        }
        if (last_date_read_model!=null) {
            last_date_read_model.save();
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
            gmail_last_date_read last_date_read_model = null;
            try {
                last_date_read_model = gmail_last_date_read.find.byId(gmail_last_date_read.constid);
            }
            catch (Exception ignored) {
            }
            final gmail_last_date_read last_date_model_f = last_date_read_model;

            Message[] messages = inbox.search(new SearchTerm() {
                @Override
                public boolean match(Message message) {
                    try {
                        if (last_date_model_f!=null) {
                            return message.getReceivedDate().after(last_date_model_f.lastdate);
                        }
                        return true;
                    }
                    catch (Exception e) {
                        Logger.error("retreiving messages (initialli)...", e);
                    }
                    return false;
                }
            });
            handle_messages(messages);
            mail_count = inbox.getMessageCount();
            inbox.addMessageCountListener(new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent messageCountEvent) {
                    mail_count += 1;
                    handle_messages(messageCountEvent.getMessages());
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