package com.google.firebase.udacity.friendlychat.messageList;

import android.view.View;

import com.google.firebase.udacity.friendlychat.model.Message;

public interface ItemClickListener {
    void onItemClick(int clickedItemIndex, View view, Message message);
}
