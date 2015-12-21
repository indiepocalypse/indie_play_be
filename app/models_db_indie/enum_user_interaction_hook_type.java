package models_db_indie;

/**
 * Created by skariel on 26/10/15.
 */
public enum enum_user_interaction_hook_type {
    NONE,
    CHANGE_ISSUE_POLICY,
    CHANGE_MERGING_POLICY,
    CHANGE_POLICY_POLICY,
    CHANGE_REPO_POLICY,
    ISSUE_CLOSE,
    DELETE_REPO,
    HI,
    LIST_ADMINS,
    LIST_COMMANDS,
    LIST_OWNERS,
    MERGE,
    NEGOTIATION_STATUS,
    OFFER_FOR_MERGE,
    ISSUE_OPEN,
    REQUEST_FOR_MERGE,
    SHOW_CREATOR,
    SHOW_POLICY,
    I_DONT_CHECK_YET_BECAUSE_IM_LAZY,
    OTHER
}
