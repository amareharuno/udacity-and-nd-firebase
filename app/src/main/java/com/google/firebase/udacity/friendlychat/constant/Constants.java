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
}
