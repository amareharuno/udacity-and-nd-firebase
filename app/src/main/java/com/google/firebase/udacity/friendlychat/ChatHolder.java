package com.google.firebase.udacity.friendlychat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.udacity.friendlychat.model.FriendlyMessage;

public class ChatHolder extends RecyclerView.ViewHolder {
    private TextView messageTextView, authorTextView;
    private ImageView photoImageView;

    public ChatHolder(View itemView) {
        super(itemView);
        messageTextView = itemView.findViewById(R.id.messageTextView);
        authorTextView = itemView.findViewById(R.id.nameTextView);
        photoImageView = itemView.findViewById(R.id.photoImageView);
    }

    public void bind(FriendlyMessage message) {
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());
    }
}
