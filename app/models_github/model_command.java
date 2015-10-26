package models_github;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by skariel on 26/10/15.
 */
public class model_command {
    public String command;
    public ArrayList<String> args;

    public model_command() {
        this.command = "";
        this.args = new ArrayList<>();
    }

    public static ArrayList<model_command> from_text(String text) {
        // basically parse the text into commands of the form @theindiepocalypse say hi /
        Pattern pattern = Pattern.compile("@theindiepocalypse\\s([\\s\\w\\.\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\[\\]\\;\\`\\~\\'\\>\\\\\\+\\=\\-\\<\\>\\,\\?\\\"\\:\\|]*)\\/");
        Matcher matcher = pattern.matcher(text);
        ArrayList<model_command> commands = new ArrayList<>();
        while(matcher.find()) {
            String[] splitted = matcher.group().split("\\s");
            if (splitted.length==0) {
                continue;
            }
            model_command command = new model_command();
            command.command = splitted[0].trim();
            for (int i=1; i<splitted.length; i++) {
                command.args.add(splitted[i].trim());
            }
            commands.add(command);
        }
        return commands;
    }

}

