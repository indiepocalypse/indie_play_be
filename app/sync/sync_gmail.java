package sync;

import com.sun.mail.imap.IMAPFolder;
import handlers.handler_general;
import models.model_gmail_last_date_read;
import models.model_ownership;
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
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class sync_gmail {
    public static int mail_count = 0;
    private static IMAPFolder inbox = null;
    private static Store mail_store = null;
    private static Thread t1 = null;
    private static Thread t2 = null;
    private static boolean interrupted = false;

    public static void start() {
        // some defensive shit here :)
        stop();

        t1 = new Thread() {
            public void run() {
                while (!interrupted) {
                    try {
                        Thread.sleep(store_conf.get_gmail_reload_sync_delta_milis());
                        Random rand = new Random();
                        int jitter = (int)(rand.nextFloat()*stores.store_conf.get_gmail_reload_sync_jitter_milis()+
                                store_conf.get_gmail_reload_sync_minimum_milis());
                        Thread.sleep(jitter);
                        reload_folder();
                    } catch (Exception e) {
                        if (!interrupted) {
                            Logger.error("while sleeping to reload inbox...", e);
                        }
                    }
                }
            }
        };

        t2 = new Thread() {
            public void run() {
                reload_folder();
                while (!interrupted) {
                    try {
                        if (inbox != null) {
                            if (inbox.isOpen()) {
                                inbox.idle(true);
                            }
                            else {
                                // try to reopen...
                                Logger.info("gmail inbox appears closed, trying to reopen...");
                                Thread.sleep(1500);
                                if (inbox.isOpen()) {
                                    Logger.info("folder is now open!");
                                    continue;
                                }
                                reload_folder();
                            }
                        }
                    } catch (Exception e) {
                        if (!interrupted) {
                            Logger.error("Error in gmail idle...", e);
                        }
                    }

                    if (interrupted) {
                        return;
                    }
                    try {
                        Thread.sleep(50);
                    }
                    catch (Exception ignored) {
                    }
                }
            }
        };

        interrupted = false;
        t2.start();
        t1.start();
    }

    public static void stop() {
        interrupted = true;
        if (t1 != null) {
            t1.interrupt();
            t1 = null;
        }
        if (t2 != null) {
            t2.interrupt();
            t2 = null;
        }
        if (inbox != null) {
            try {
                inbox.close(true);
            } catch (Exception e) {
                Logger.error("while closing inbox...", e);
            }
            inbox = null;
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

    private static void handle_messages(Message[] ms) {
        model_gmail_last_date_read last_date_read_model = store_local_db.get_gmail_latest_sync_date();
        for (Message m : ms) {
// TODO: FIX: THIS JITTER SLEEP HERE CAUSES FLODER TO CLOSE?
//            try {
//                Thread.sleep(stores.store_conf.get_gmail_reload_sync_jitter_small_milis());
//            }
//            catch (Exception ignored) {
//            }
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

            if ((m_from != null) && (m_from.equals("GitHub <support@github.com>"))) {
                if (m_subject.contains("Repository transfer from")) {
                    String from_user = m_subject.split("@")[1].split("\\s+")[0];
                    String repo_name = m_subject.split("/")[1].split("\\)")[0];
                    Logger.info("transfering from user: " + from_user + "    repo name: " + repo_name);
                    String lines[] = m_body.split("\\r?\\n");
                    for (String l : lines) {
                        String lt = l.trim();
                        if (lt.startsWith("https")) {
                            // try to accept the repo!

                            if (store_local_db.has_repo(repo_name)) {
                                continue;
                            }
                            model_ownership ownership = handler_general.integrate_github_repo(repo_name, from_user, false);
                            if (ownership == null) {
                                // we try once more...
                                ownership = handler_general.integrate_github_repo(repo_name, from_user, false);
                                if (ownership == null) {
                                    // just report...
                                    Logger.error("Problem transferring repo \"" + repo_name + "\" into DB");
                                    continue;
                                }
                            }
                            if (!store_github_iojs.accept_trasfer_repo(lt)) {
                                // unsuccesfull transfer, report
                                Logger.error("Problem transferring repo \"" + repo_name + "\" from Github");
                                continue;
                            }
                            // all seems ok!
                            try {
                                Thread.sleep(5100);
                            }
                            catch (Exception ignored) {
                            }
                            store_github_api.create_webhook(ownership.repo);
                            if (store_github_api.delete_collaborator_from_repo(ownership.repo.repo_name, from_user)) {
                                Logger.info("user "+from_user+" removed from collaborators to "+ownership.repo.repo_name);
                            }
                            else {
                                Logger.error("could not remove user "+from_user+" removed from collaborators to "+ownership.repo.repo_name);
                            }

                            Logger.info("Successfuly transferred repo \"" + repo_name + "\" from Github");
                        }
                    }
                }
            }
        }
        if (last_date_read_model != null) {
            store_local_db.update_gmail_last_read_date(last_date_read_model);
        }
    }

    private static void reload_folder() {
        try {
            if (inbox != null) {
                inbox.close(true);
                inbox = null;
            }
            if (mail_store != null) {
                mail_store.close();
                mail_store = null;
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
            final model_gmail_last_date_read f_last_date_read_model = store_local_db.get_gmail_latest_sync_date();

            Message[] messages = inbox.search(new SearchTerm() {
                @Override
                public boolean match(Message message) {
                    try {
                        return f_last_date_read_model==null || message.getReceivedDate().after(f_last_date_read_model.lastdate);
                    } catch (Exception e) {
                        Logger.error("retreiving messages (initialli)...", e);
                    }
                    return false;
                }
            });
            mail_count = inbox.getMessageCount();
            handle_messages(messages);
            Logger.info("mail_count"+Integer.toString(mail_count));
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
    }
}