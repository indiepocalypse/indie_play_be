package sync;

import com.sun.mail.imap.IMAPFolder;
import models.model_gmail_last_date_read;
import models.model_repo;
import models.model_user;
import play.Logger;
import stores.*;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class sync_gmail {
    public static int mail_count = 0;
    static IMAPFolder inbox = null;
    static Store mail_store = null;
    static Thread t1 = null;
    static Thread t2 = null;
    static boolean reloading = false;

    public static void start() {
        if (t1 == null) {
            t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            Thread.sleep(store_conf.get_gmail_reload_sync_delta_milis());
                            Random rand = new Random();
                            int jitter = (int)(rand.nextFloat()*stores.store_conf.get_gmail_reload_sync_jitter_milis()+
                                    store_conf.get_gmail_reload_sync_minimum_milis());
                            Thread.sleep(jitter);
                            reload_folder();
                        } catch (Exception e) {
                            Logger.error("while sleeping to reload inbox...", e);
                        }
                    }
                }
            };
            t1.start();
        }

        if (t2 == null) {
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
    }

    public static void stop() {
        if (t1 != null) {
            t1.interrupt();
            t1 = null;
        }
        if (inbox != null) {
            try {
                inbox.close(true);
            } catch (Exception e) {
                Logger.error("while closing inbox...", e);
            }
            inbox = null;
        }
        if (t2 != null) {
            t2.interrupt();
            t2 = null;
        }
        if (mail_store != null) {
            try {
                mail_store.close();
            } catch (Exception e) {
                Logger.error("while closing inbox...", e);
            }
            mail_store = null;
        }
    }


    public static void handle_messages(Message[] ms) {
        // TODO: move last date stuff into the sotre!
        model_gmail_last_date_read last_date_read_model = null;
        boolean should_save_date;
        try {
            last_date_read_model = model_gmail_last_date_read.find.byId(model_gmail_last_date_read.constid);
        } catch (Exception ignored) {
        }
        if (last_date_read_model == null) {
            should_save_date = true;
        } else {
            should_save_date = false;
        }
        for (Message m : ms) {
            try {
                Thread.sleep(stores.store_conf.get_gmail_reload_sync_jitter_milis());
            }
            catch (Exception ignored) {
            }
            Date m_date = null;
            String m_subject = null;
            String m_body = null;
            String m_from = null;
            try {
                m_date = m.getReceivedDate();
                m_subject = m.getSubject();
                try {
                    m_body = (String) m.getContent();
                } catch (Exception ignored_this_one_too) {
                    m_body = "just something";
                }
                m_from = m.getFrom()[0].toString();
            } catch (Exception e) {
                Logger.error("while reading message date...", e);
            }
            if ((m_date == null) || (m_subject == null) || (m_body == null)) {
                continue;
            }
            if (last_date_read_model == null) {
                last_date_read_model = new model_gmail_last_date_read(m_date);
            } else {
                if (last_date_read_model.lastdate.before(m_date)) {
                    last_date_read_model.lastdate = m_date;
                }
            }

            // TODO: actually handle the message....
            if ((m_from != null) && (m_from.equals("GitHub <support@github.com>"))) {
                if ((m_subject != null) && (m_subject.contains("Repository transfer from"))) {
                    if (m_body != null) {
                        String from_user = m_subject.split("@")[1].split("\\s+")[0];
                        String repo_name = m_subject.split("/")[1].split("\\)")[0];
                        Logger.info("transfering from user: " + from_user + "    repo name: " + repo_name);
                        String lines[] = m_body.split("\\r?\\n");
                        for (String l : lines) {
                            String lt = l.trim();
                            if (lt.startsWith("https")) {
                                // accept the repo!
                                if (store_github_iojs.accept_trasfer_repo(lt)) {
                                    model_repo repo = store_github_api.get_repo_by_name(from_user, repo_name);
                                    model_user user = store_github_api.get_user_by_name(from_user);
                                    store_local_db.register_transfered_repo(user, repo);
                                } else {
                                    // TODO: handle unsuccesful transfer! or ignore ;)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (last_date_read_model != null) {
            if (should_save_date) {
                last_date_read_model.save();
            } else {
                last_date_read_model.update();
            }
        }
    }

    public static void reload_folder() {
        if (reloading) {
            return;
        }
        reloading = true;

        try {
            if (inbox != null) {
                inbox.close(true);
            }
            if (mail_store != null) {
                mail_store.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            mail_store = imap_session.getStore("imaps");
            mail_store.connect("imap.gmail.com", store_credentials.gmail.name, store_credentials.gmail.pssw);
            inbox = (IMAPFolder) mail_store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            model_gmail_last_date_read last_date_read_model = null;
            try {
                last_date_read_model = model_gmail_last_date_read.find.byId(model_gmail_last_date_read.constid);
            } catch (Exception ignored) {
            }
            final model_gmail_last_date_read last_date_model_f = last_date_read_model;

            Message[] messages = inbox.search(new SearchTerm() {
                @Override
                public boolean match(Message message) {
                    try {
                        if (last_date_model_f != null) {
                            return message.getReceivedDate().after(last_date_model_f.lastdate);
                        }
                        return true;
                    } catch (Exception e) {
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
        } catch (Exception e) {
            Logger.error("while reloading gmail inbox... ", e);
        }
        reloading = false;
    }
}