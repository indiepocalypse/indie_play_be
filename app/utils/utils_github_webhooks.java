package utils;

import models_github.enum_webhook_action;

/**
 * Created by skariel on 26/10/15.
 */
public class utils_github_webhooks {
    public static enum_webhook_action from_string(String action) {
        if (action.equals("opened")) {
            return enum_webhook_action.OPENED;
        }
        if (action.equals("closed")) {
            return enum_webhook_action.CLOSED;
        }
        if (action.equals("created")) {
            return enum_webhook_action.CREATED;
        }
        if (action.equals("synchronize")) {
            return enum_webhook_action.SYNCHRONIZE;
        }
        return enum_webhook_action.OTHER;
    }
}
