package models_memory_indie;

import controllers.routes;
import play.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by skariel on 26/10/15.
 */
public class model_command {
    @Nonnull
    public final ArrayList<String> args;
    @Nonnull
    public final String command;

    private model_command(
            @Nonnull ArrayList<String> p_args,
            @Nonnull String p_command) {
        assert p_command != null;
        assert p_args != null;

        this.command = p_command;
        this.args = p_args;
    }

    public static ArrayList<model_command> from_text(@Nonnull String text) {
        assert text != null;

        // basically parse the text into commands of the form @theindiepocalypse say hi /
        text += "/";
        Pattern pattern = Pattern.compile("@theindiepocalypse\\s([\\s\\w\\.\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\[\\]" +
                "\\;\\`\\~\\'\\>\\\\\\+\\=\\-\\<\\>\\,\\?\\\"\\:\\|]*)\\/");
        Matcher matcher = pattern.matcher(text);
        ArrayList<model_command> commands = new ArrayList<>();
        while (matcher.find()) {
            String match = matcher.group().trim();
            Logger.info("---- match=" + match);
            String clean_match = match
                    .replace("@theindiepocalypse", "")
                    .replace("/", "")
                    .trim();
            String[] splitted2 = clean_match.split("\\s+", 1);
            String[] splitted = clean_match.split("\\s+");
            if (splitted.length == 0) {
                continue;
            }
            ArrayList<String> args = new ArrayList<>(3);
            String cmd = "";
            String joined_args = "";
            cmd = splitted[0];
            if (splitted2.length > 1) {
                joined_args = splitted2[1];
            }
            args.addAll(Arrays.asList(splitted).subList(1, splitted.length));
            model_command command = new model_command(args, cmd);
            commands.add(command);
        }
        return commands;
    }

}

