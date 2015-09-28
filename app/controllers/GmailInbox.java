package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;

public class GmailInbox {

    public static String read() {


        String tmp_name = ConfigFactory.load().getString("credentials.indie.gmail.username");
        String tmp_pssw = ConfigFactory.load().getString("credentials.indie.gmail.pssw");
        System.out.println("tmp_name="+tmp_name);
        System.out.println("tmp_pssw="+tmp_pssw);

        try {
            JsonNode json = play.libs.Json.parse(new FileInputStream("app/controllers/.gmail_indie_credentials_local_secret"));
            tmp_name = json.get("username").asText();
            tmp_pssw = json.get("pssw").asText();
        }
        catch (FileNotFoundException ignored) {
        }

        final Properties properties = new Properties();
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.imap.socketFactory.fallback", "false");
        String subject = null;
        try {
            Session imap_session = Session.getDefaultInstance(properties, null);
            imap_session.setDebug(false);

            Store store = imap_session.getStore("imaps");

            store.connect("imap.gmail.com", tmp_name, tmp_pssw);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);
            int messageCount = inbox.getMessageCount();

            System.out.println("Total Messages:- " + messageCount);

            Message[] messages = inbox.getMessages();
            System.out.println("------------------------------");
            for (int i = 0; i < 10; i++) {
                System.out.println("Mail Subject:- " + messages[i].getSubject());
                subject += messages[i].getSubject();
            }
            inbox.close(true);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return subject;
    }

}