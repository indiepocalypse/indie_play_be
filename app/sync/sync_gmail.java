package sync;

import com.sun.mail.imap.IMAPFolder;
import handlers.handler_general;
import handlers.handler_policy;
import models_db_indie.model_gmail_last_date_read;
import play.Logger;
import stores.*;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;
import java.util.Date;
import java.util.Properties;

public class sync_gmail {
    // TODO: refactor most of gmail functionality onto a store_gmail_api
    // FIXME: ?? there's a bug transferring `just_testing_game` it remains on skariel github account?
    private static int mail_count = 0;
    private static IMAPFolder inbox = null;
    private static Store mail_store = null;
    private static Thread t1 = null;
    private static Thread t2 = null;
    private static boolean interrupted = false;
    private static Session imap_session = null; // used to get mail
    private static Session smtp_session = null; // used to send mail

    public static void start() {
        // some defensive shit here :)
        stop();

        t1 = new Thread() {
            public void run() {
                while (!interrupted) {
                    try {
                        if (!interrupted) {
                            Thread.sleep(store_conf.get_delay_L2_milis());
                        }
                        if (!interrupted) {
                            reload_folder();
                        }
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
                            } else {
                                // try to reopen...
                                Logger.info("gmail inbox appears closed, trying to reopen...");
                                if (!interrupted) {
                                    Thread.sleep(store_conf.get_delay_L1_milis());
                                } else {
                                    return;
                                }
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
                        if (!interrupted) {
                            Thread.sleep(store_conf.get_delay_L1_milis());
                        } else {
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        };

        interrupted = false;
        t2.start();
        t1.start();
    }

    private static void stop() {
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
        imap_session = null;
    }

    private static void handle_messages(Message[] ms) {
        model_gmail_last_date_read last_date_read_model = store_local_db.get_gmail_latest_sync_date();
        for (Message m : ms) {
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
            } catch (FolderClosedException e) {
                Logger.error("folder is close while handling gmail message, returning (error follows)\n", e);
                return;
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
                    store_local_db.update_gmail_last_read_date(last_date_read_model);
                }
            }

            if ((m_from != null) && (m_from.equals("GitHub <support@github.com>"))) {
                if (m_subject.contains("Repository transfer from")) {
                    String from_user = m_subject.split("@")[1].split("\\s+")[0];
                    String repo_name = m_subject.split("/")[1].split("\\)")[0];
                    if (!handler_policy.can_create_new_repo(from_user)) {
                        Logger.info("cannot transfer repo " + repo_name + " from user: " + from_user + " because of policy of maximum repos with more than 50% ownership.");
                        String user_mail;
                        try {
                            user_mail = store_github_api.get_user_mail(from_user);
                        } catch (github_io_exception e) {
                            Logger.error("could not get user " + from_user + " mail in order to send a cannot move repo error. Repo name is " + repo_name);
                            continue;
                        }
                        final String mail_subject = "Cannot accept repository transfer (" + repo_name + ")";
                        final String mail_body = "The reason is that you already have the maximum number of repos allowed with 50% ore more ownership.";
                        sendmail(user_mail, mail_subject, mail_body);
                        continue;
                    }
                    Logger.info("transfering from user: " + from_user + "    repo name: " + repo_name);
                    String lines[] = m_body.split("\\r?\\n");
                    for (String l : lines) {
                        String lt = l.trim();
                        if (lt.startsWith("https")) {

                            // try to accept the repo!

                            if (store_local_db.has_repo(repo_name)) {
                                Logger.info("cannot transfer repo " + repo_name + " from user: " + from_user + " because it already exists in DB!");
                                String user_mail = null;
                                try {
                                    user_mail = store_github_api.get_user_mail(from_user);
                                } catch (github_io_exception e) {
                                    Logger.error("Could not get user " + from_user + " mail to send him a mail about error that repo " + repo_name + " cannot be transferred because a similar name already exists in the db");
                                }
                                final String mail_subject = "Cannot accept repository transfer (" + repo_name + ")";
                                final String mail_body = "The reason is that there is a repo with the same name in the DB.";
                                sendmail(user_mail, mail_subject, mail_body);
                                break;
                            }
                            // delay needed to let github spread news that user wants to transfer repo
                            try {
                                if (!interrupted) {
                                    Thread.sleep(store_conf.get_delay_L1_milis());
                                } else {
                                    return;
                                }
                            } catch (Exception ignored) {
                            }
                            if (!store_github_iojs.accept_transfer_repo(lt)) {
                                // unsuccesfull transfer, report
                                Logger.error("Problem transferring repo \"" + repo_name + "\" from Github");
                                break;
                            }
                            // all seems ok!
                            try {
                                if (!interrupted) {
                                    Thread.sleep(store_conf.get_delay_L1_milis());
                                } else {
                                    return;
                                }
                            } catch (Exception ignored) {
                            }
                            // we need the above delay to let github spread the news that repo was transferred
                            try {
                                final boolean check_for_existance_of_readme = true;
                                final boolean create_webhook = true;
                                final boolean delete_original_collaborators = true;
                                handler_general.integrate_github_repo_that_was_transferred(repo_name, from_user, create_webhook,
                                        check_for_existance_of_readme, delete_original_collaborators);
                            } catch (Exception e) {
                                Logger.error("XX--> cannot move repo " + repo_name + " from user " + from_user + ". Maybe it has been deleted? -->\n", e);
                                break;
                            }

                            // TODO: determine real success?
                            Logger.info("code for transfer of repo \"" + repo_name + "\" is done. check above for any errors.");
                        }
                    }
                }
            }
        }
        if (last_date_read_model != null) {
            store_local_db.update_gmail_last_read_date(last_date_read_model);
        }
    }

    public static void sendmail(String user_mail, String mail_subject, String mail_body) {
        if (!store_conf.get_debug_should_send_mails()) {
            Logger.warn("SKIPPING MAIL BECAUSE DEBUG:\n" +
                    "to: " + user_mail +
                    "subject: " + mail_subject +
                    "body: " + mail_body);
        } else {
            // TODO: move this method to a store_gmail_api (which does not exist yet...)
            Message message = new MimeMessage(smtp_session);
            try {
                message.setSubject(mail_subject);
                InternetAddress address = new InternetAddress(store_conf.get_indie_mail_address());
                address.setPersonal("theindiepocalypse");
                message.setFrom(address);
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(user_mail));
                message.setText(mail_body);
                Transport.send(message);
            } catch (Exception e) {
                Logger.error("while sending mail saying cannot transfer repo: ", e);
            }
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

        final Properties imap_properties = System.getProperties();
        imap_properties.put("mail.imap.ssl.enable", "true");
        imap_properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        imap_properties.put("mail.imap.socketFactory.fallback", "false");

        imap_properties.setProperty("mail.imap.host", "imap.gmail.com");
        imap_properties.setProperty("mail.imap.port", "993");
        imap_properties.setProperty("mail.imap.connectiontimeout", "5000");
        imap_properties.setProperty("mail.imap.timeout", "5000");

        imap_session = Session.getDefaultInstance(imap_properties, null);
        imap_session.setDebug(false);


        Properties smtp_properties = new Properties();
        smtp_properties.put("mail.smtp.auth", "true");
        smtp_properties.put("mail.smtp.starttls.enable", "true");
        smtp_properties.put("mail.smtp.host", "smtp.gmail.com");
        smtp_properties.put("mail.smtp.port", "587");

        smtp_session = Session.getInstance(smtp_properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(store_credentials.get_gmail_indie_user_name(), store_credentials.get_gmail_indie_password());
                    }
                });

        try {
            mail_store = imap_session.getStore("imaps");
            mail_store.connect("imap.gmail.com", store_credentials.get_gmail_indie_user_name(), store_credentials.get_gmail_indie_password());
            inbox = (IMAPFolder) mail_store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            final model_gmail_last_date_read f_last_date_read_model = store_local_db.get_gmail_latest_sync_date();

            Message[] messages = inbox.search(new SearchTerm() {
                @Override
                public boolean match(Message message) {
                    try {
                        return f_last_date_read_model == null || message.getReceivedDate().after(f_last_date_read_model.lastdate);
                    } catch (Exception e) {
                        Logger.error("retreiving messages (initialli)...", e);
                    }
                    return false;
                }
            });
            mail_count = inbox.getMessageCount();
            handle_messages(messages);
            Logger.info("mail_count" + Integer.toString(mail_count));
            if (inbox == null) {
                Logger.error("inbox null while reloading gmail messages, returning");
                return;
            }
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
