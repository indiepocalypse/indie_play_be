package models_db_indie;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheStrategy;
import commands.interface_command;
import models_memory_github.interface_github_webhook;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by skariel on 29/09/15.
 */
@CacheStrategy(readOnly = true, warmingQuery = "order by id")
@Entity
public class model_user_interaction extends Model {
    // no relations in this class to not break schemas. This class is a historic record, its a bit special!
    // ie other entities can be safely deleted, while these stay on the DB
    private static final Finder<String, model_user_interaction> find = new Finder<>(model_user_interaction.class);
    public final String user_name;
    @Id
    private final String id;
    final Date date_performed;
    final enum_user_interaction_hook_type hook_interaction_type;
    final enum_user_interaction_web_type web_interaction_type;
    final enum_user_interaction_mail_type mail_interaction_type;
    // some free parameters :)
    final String p1;
    final String p1_desc;
    final String p2;
    final String p2_desc;
    final String p3;
    final String p3_desc;
    final String p4;
    final String p4_desc;
    final String p5;
    final String p5_desc;

    private model_user_interaction(
            final String p_user_name,
            final enum_user_interaction_hook_type p_hook_interaction_type,
            final enum_user_interaction_web_type p_web_interaction_type,
            final enum_user_interaction_mail_type p_mail_interaction_type,
            final String p_p1,
            final String p_p1_desc,
            final String p_p2,
            final String p_p2_desc,
            final String p_p3,
            final String p_p3_desc,
            final String p_p4,
            final String p_p4_desc,
            final String p_p5,
            final String p_p5_desc
    ) {
        this.date_performed = new Date();
        // yeah, the user could make two actions at the same time (say if the server clock is manipulated, reset, or whatever). That's good enough though...
        this.id = p_user_name + "@action@" + date_performed.toString();
        this.user_name = p_user_name;
        this.hook_interaction_type = p_hook_interaction_type;
        this.web_interaction_type = p_web_interaction_type;
        this.mail_interaction_type = p_mail_interaction_type;
        this.p1 = p_p1;
        this.p1_desc = p_p1_desc;
        this.p2 = p_p2;
        this.p2_desc = p_p2_desc;
        this.p3 = p_p3;
        this.p3_desc = p_p3_desc;
        this.p4 = p_p4;
        this.p4_desc = p_p4_desc;
        this.p5 = p_p5;
        this.p5_desc = p_p5_desc;
    }

    public static Query<model_user_interaction> fetch() {
        return find.setUseQueryCache(true));
    }

    // TODO: make a more fine grained resolution here... ie a method for specific commands
    public model_user_interaction from_general_command(final interface_command command, final interface_github_webhook hook) {
        final String p_user_name = hook.get_user().user_name;
        final enum_user_interaction_hook_type p_hook_interaction_type = enum_user_interaction_hook_type.I_DONT_CHECK_YET_BECAUSE_IM_LAZY;
        final enum_user_interaction_web_type p_web_interaction_type = enum_user_interaction_web_type.NONE;
        final enum_user_interaction_mail_type p_mail_interaction_type = enum_user_interaction_mail_type.NONE;
        final String p_p1 = command.get_command_name();
        final String p_p1_desc = "command name";
        final String p_p2 = hook.get_repo().repo_name;
        final String p_p2_desc = "repo name";
        final String p_p3 = hook.get_issue().number;
        final String p_p3_desc = "issue number";
        String tmp1 = null;
        if (hook.get_pull_request()!=null) {
            tmp1 = "true";
        }

        final String p_p4 = tmp1;
        final String p_p4_desc = "issue is pull request";
        final String p_p5 = null;
        final String p_p5_desc = null;

        return new model_user_interaction(
            p_user_name,
            p_hook_interaction_type,
            p_web_interaction_type,
            p_mail_interaction_type,
            p_p1, p_p1_desc,
            p_p2, p_p2_desc,
            p_p3, p_p3_desc,
            p_p4, p_p4_desc,
            p_p5, p_p5_desc
        );
    }

    // TODO: extract more data here: file_name if uploaded, repo name if created, etc.
    public model_user_interaction from_web(final String p_user_name, final enum_user_interaction_web_type web_interaction_type) {
        final enum_user_interaction_hook_type p_hook_interaction_type = enum_user_interaction_hook_type.NONE;
        final enum_user_interaction_web_type p_web_interaction_type = web_interaction_type;
        final enum_user_interaction_mail_type p_mail_interaction_type = enum_user_interaction_mail_type.NONE;
        final String p_p1 = null;
        final String p_p1_desc = null;
        final String p_p2 = null;
        final String p_p2_desc = null;
        final String p_p3 = null;
        final String p_p3_desc = null;
        final String p_p4 = null;
        final String p_p4_desc = null;
        final String p_p5 = null;
        final String p_p5_desc = null;

        return new model_user_interaction(
            p_user_name,
            p_hook_interaction_type,
            p_web_interaction_type,
            p_mail_interaction_type,
            p_p1, p_p1_desc,
            p_p2, p_p2_desc,
            p_p3, p_p3_desc,
            p_p4, p_p4_desc,
            p_p5, p_p5_desc
        );
    }

    xxxxxxxxxxxxxxxxxxxxxxx // TODO: implement this one
    public model_user_interaction from_mail(final String p_user_name, final enum_user_interaction_web_type web_interaction_type) {
        final enum_user_interaction_hook_type p_hook_interaction_type = enum_user_interaction_hook_type.NONE;
        final enum_user_interaction_web_type p_web_interaction_type = web_interaction_type;
        final enum_user_interaction_mail_type p_mail_interaction_type = enum_user_interaction_mail_type.NONE;
        final String p_p1 = null;
        final String p_p1_desc = null;
        final String p_p2 = null;
        final String p_p2_desc = null;
        final String p_p3 = null;
        final String p_p3_desc = null;
        final String p_p4 = null;
        final String p_p4_desc = null;
        final String p_p5 = null;
        final String p_p5_desc = null;

        return new model_user_interaction(
            p_user_name,
            p_hook_interaction_type,
            p_web_interaction_type,
            p_mail_interaction_type,
            p_p1, p_p1_desc,
            p_p2, p_p2_desc,
            p_p3, p_p3_desc,
            p_p4, p_p4_desc,
            p_p5, p_p5_desc
        );
    }
}

