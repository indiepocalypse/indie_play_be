package models_github;

import play.Logger;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by skariel on 26/10/15.
 */
public class model_command {
    public String command;
    public String joined_args;
    public ArrayList<String> args;

    public model_command() {
        this.command = "";
        this.joined_args = "";
        this.args = new ArrayList<>();
    }

    public static ArrayList<model_command> from_text(String text) {
        // basically parse the text into commands of the form @theindiepocalypse say hi /
        String s = "\\Z";
        Pattern pattern = Pattern.compile("@theindiepocalypse\\s([\\s\\w\\.\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\[\\]"+
                "\\;\\`\\~\\'\\>\\\\\\+\\=\\-\\<\\>\\,\\?\\\"\\:\\|]*)[\\/"+s+"]");
        Matcher matcher = pattern.matcher(text);
        ArrayList<model_command> commands = new ArrayList<>();
        while(matcher.find()) {
            String match = matcher.group().trim();
            Logger.info("---- match="+match);
            String clean_match = match
                    .replace("@theindiepocalypse","")
                    .replace("/","")
                    .trim();
            String[] splitted2 = clean_match.split("\\s+", 1);
            String[] splitted = clean_match.split("\\s+");
            if (splitted.length==0) {
                continue;
            }
            model_command command = new model_command();
            command.command = splitted[0];
            if (splitted2.length>1) {
                command.joined_args = splitted2[1];
            }
            for (int i=1; i<splitted.length; i++) {
                command.args.add(splitted[i]);
            }
            commands.add(command);
        }
        return commands;
    }

}

