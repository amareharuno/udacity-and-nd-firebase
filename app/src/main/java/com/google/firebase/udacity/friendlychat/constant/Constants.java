package com.google.firebase.udacity.friendlychat.constant;

public final class Constants {
    public static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";

    // default message length
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 300;
    // key to message length parameter from firebase
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    // Activity result codes
    public static final int RC_SIGN_IN = 1;
    public static final int RC_PHOTO_PICKER = 2;

    // Context message menu
    public static final int MENU_ITEM_ID_EDIT_MESSAGE = 1;
    public static final int MENU_ITEM_ID_REMOVE_MESSAGE = 2;
    public static final int MENU_ITEM_ID_COPY_MESSAGE_TEXT = 3;

    public static final String APP_ID_FOR_ADDMOB = "ca-app-pub-3450207078907523~9818432930";

    // DB references
    public static final String MESSAGES_DATABASE_REFERENCE = "messages";
    public static final String CHAT_PHOTOS_REFERENCE = "chat_photos";

    // Toast messages
    public static final String TOAST_ERROR_GETTING_DATA = "Error while getting data";
    public static final String TOAST_SIGNED_IN = "Signed in!";
    public static final String TOAST_SIGNED_IN_CANCELED = "Sign in canceled";
    public static final String TOAST_IMAGE_WAS_NOT_UPLOADED = "Image wasn't uploaded";
    public static final String TOAST_TEXT_SAVED_TO_CLIPBOARD = "Message text saved to clipboard";
    public static final String TOAST_ERROR_SAVING_TO_CLIPBOARD = "Error trying to save to clipboard";

    public static final String ERROR_FETCHING_CONFIG = "Error fetching config";

    public static final String EMPTY_STRING = "";

    public static final String PHOTO_PICKER_FORMAT = "image/jpeg";
    public static final String CHOOSE_IMAGE = "Choose image";

    // For remote config
    public static final int CACHE_EXPIRATION = 3600;
    public static final int CACHE_EXPIRATION_FOR_DEBUG = 0;

    public static final String MESSAGE_CONTEXT_MENU_TITLE = "Message";
}
